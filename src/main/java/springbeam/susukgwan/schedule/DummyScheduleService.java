package springbeam.susukgwan.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service
public class DummyScheduleService {
    @Autowired
    private TutoringRepository tutoringRepository;
    @Autowired
    private IrregularRepository irregularRepository;
    @Autowired
    private CancellationRepository cancellationRepository;
    @Autowired
    private RegularRecordRepository regularRecordRepository;

    public void newDummyIrregularSchedule(Tutoring targetTutoring, LocalDate date) {
        // compare day, startTime, endTime of irregular schedule with regular schedule
        // to check if it is preoccupied or not.
        List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(targetTutoring.getTutorId());
        DayOfWeek day = date.getDayOfWeek();
        LocalTime startTime = LocalTime.of(0, 0);
        LocalTime endTime = LocalTime.of(0, 5);
        for (Tutoring tutoring: tutoringList) {
            // if the dateTime is preoccupied by a certain regular time, return that time.
            Time time = isPreoccupiedDateTime(tutoring, date, day, startTime, endTime);
            if (time != null) {
                return;
            }
        }

        // check all irregular times
        List<Irregular> irregularList = irregularRepository.findAllByTutorId(targetTutoring.getTutorId());
        for (Irregular irregular: irregularList) {
            if (date.isEqual(irregular.getDate()) &&
                    isOverlapped(startTime, endTime, irregular.getStartTime(), irregular.getEndTime()))
            {
                return;
            }
        }
        // save irregular time schedule
        Irregular newIrregular = Irregular.builder().date(date).startTime(startTime).endTime(endTime)
                .tutoring(targetTutoring).tutorId(targetTutoring.getTutorId()).build();
        irregularRepository.save(newIrregular);
    }

    /* 비정규일정의 등록, 일정 변경에 사용됨. */
    private Time isPreoccupiedDateTime(Tutoring tutoring, LocalDate date, DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        // 확인하려는 것이 미래에 시작하는 일정일 경우 수업의 현재 정규시간을 확인하면 됨.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        List<Time> timeList = tutoring.getTimes(); // 수업 정규시간
        if (startDateTime.isAfter(now)) {
            for (Time time: timeList) {
                if (day.equals(time.getDay()) &&
                        isOverlapped(startTime, endTime, time.getStartTime(), time.getEndTime()) &&
                        cancellationRepository.findAllByCancelledDateTime(LocalDateTime.of(date, time.getStartTime())).isEmpty())
                { // 정규시간과 요일 및 시간이 겹치고, 그 날에 정규수업 취소가 되어있지 않은 경우에 겹치는 시간 반환.
                    return time;
                }
            }
        }
        else {
            // 과거의 일정일 경우 현재정규시간, 과거정규기록 중 어느 것이 적용되는 시간대인지 확인
            // 적용시간대가 오래된 것이 앞에 오도록 정렬됨. ~OrderBy~Asc
            List<RegularRecord> regularRecords = regularRecordRepository.findAllByTutoringOrderByAppliedUntilAsc(tutoring);
            // 과거 마지막 정규기록 ~ 현재 정규시간인 경우 혹은 정규기록이 없는 경우에는 현재 정규시간이 적용됨.
            List<Time> appliedTimeList = timeList;
            for (RegularRecord regularRecord: regularRecords) {
                if (regularRecord.getAppliedUntil().isAfter(startDateTime)) {
                    // 이 정규시기가 적용되던 기간에 시작한 경우이므로, 이 처음 매칭되는 정규타임을 적용해야 함.
                    appliedTimeList = parseDayTimeString(regularRecord.getDayTimeString(), tutoring);
                    break;
                }
            }
            // 당시의 정규시간과 요일 및 시간이 겹치고, 그 날에 정규수업 취소가 되어있지 않은 경우에 겹치는 시간 반환.
            for (Time time: appliedTimeList) {
                if (day.equals(time.getDay()) &&
                        isOverlapped(startTime, endTime, time.getStartTime(), time.getEndTime()) &&
                        cancellationRepository.findAllByCancelledDateTime(LocalDateTime.of(date, time.getStartTime())).isEmpty())
                {
                    return time;
                }
            }
        }
        // 안 겹치는 경우
        return null;
    }

    private boolean isOverlapped(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        // true if the two regular times are overlapped
        return !(s1.isAfter(e2) || s2.isAfter(e1));
    }
    private List<Time> parseDayTimeString(String dayTimeString, Tutoring tutoring) {
        String[] split = dayTimeString.split(",");
        Iterator<String> it = Arrays.stream(split).iterator();
        List<Time> timeList = new ArrayList<>();
        while (it.hasNext()) {
            String[] each = it.next().strip().split(" ");
            DayOfWeek dayOfWeek = DayOfWeek.of(Integer.parseInt(each[0]));
            LocalTime startTime = LocalTime.parse(each[1]);
            LocalTime endTime = LocalTime.parse(each[2]);
            Time regularTime = Time.builder().day(dayOfWeek).startTime(startTime).endTime(endTime).tutoring(tutoring).build();
            timeList.add(regularTime);
        }
        return timeList;
    }
}
