package springbeam.susukgwan.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.fcm.PushService;
import springbeam.susukgwan.schedule.dto.ChangeRegularDTO;
import springbeam.susukgwan.schedule.dto.GetScheduleDTO;
import springbeam.susukgwan.schedule.dto.ScheduleDTO;
import springbeam.susukgwan.schedule.dto.ScheduleInfoResponseDTO;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScheduleService {
    @Autowired
    private IrregularRepository irregularRepository;
    @Autowired
    private CancellationRepository cancellationRepository;
    @Autowired
    private TutoringRepository tutoringRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private PushService pushService;

    public ResponseEntity<?> newIrregularSchedule(ScheduleDTO scheduleDTO) {
        // Check whether the request user actually has this tutoring.
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(scheduleDTO.getTutoringId(), tutorId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }
        Tutoring targetTutoring = tutoringOptional.get();

        // compare day, startTime, endTime of irregular schedule with regular schedule
        // to check if it is preoccupied or not.
        List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(tutorId);
        LocalDate date = LocalDate.parse(scheduleDTO.getDate(), DateTimeFormatter.ISO_DATE);
        DayOfWeek day = date.getDayOfWeek();
        LocalTime startTime = LocalTime.parse(scheduleDTO.getStartTime(), DateTimeFormatter.ISO_LOCAL_TIME);
        LocalTime endTime = LocalTime.parse(scheduleDTO.getEndTime(), DateTimeFormatter.ISO_LOCAL_TIME);
        for (Tutoring tutoring: tutoringList) {
            // if the dateTime is preoccupied by a certain regular time, return that time.
            Time time = isPreoccupiedDateTime(tutoring, date, day, startTime, endTime);
            if (time != null) {
                String tuteeName = "";
                // get one's name if tutee is registered
                if (tutoring.getTuteeId()!=null && userRepository.findById(tutoring.getTuteeId()).isPresent()) {
                    Optional<User> tuteeOptional = userRepository.findById(tutoring.getTuteeId());
                    tuteeName = tuteeOptional.get().getName();
                }
                String subjectName = tutoring.getSubject().getName();
                String errorMessage = tuteeName + " 학생 " + subjectName + " 수업과 겹칩니다. (" + time.getStartTime().toString() + "~" + time.getEndTime().toString() + ")";
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(errorMessage));
            }
        }

        // check all irregular times
        List<Irregular> irregularList = irregularRepository.findAllByTutorId(tutorId);
        for (Irregular irregular: irregularList) {
            if (date.isEqual(irregular.getDate()) &&
                    isOverlapped(startTime, endTime, irregular.getStartTime(), irregular.getEndTime()))
            {
                String tuteeName = "";
                Tutoring overlappedTutoring = irregular.getTutoring(); // tutoring 존재 x의 경우는 일단 무시 tutoring 삭제 시 정규취소, irregular 취소 모두 삭제되도록 해야 됨.
                // get one's name if tutee is registered
                if (overlappedTutoring.getTuteeId()!=null && userRepository.findById(overlappedTutoring.getTuteeId()).isPresent()) {
                    Optional <User> tuteeOptional = userRepository.findById(overlappedTutoring.getTuteeId());
                    tuteeName = tuteeOptional.get().getName();
                }
                String subjectName = overlappedTutoring.getSubject().getName();
                String errorMessage = tuteeName + " 학생 " + subjectName + " 수업과 겹칩니다. (" + date.toString() + " " + irregular.getStartTime().toString() + "~" + irregular.getEndTime().toString() + ")";
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
            }
        }
        // save irregular time schedule
        Irregular newIrregular = Irregular.builder().date(date).startTime(startTime).endTime(endTime)
                .tutoring(targetTutoring).tutorId(tutorId).build();
        irregularRepository.save(newIrregular);
        pushService.newIrregularScheduleNotification(targetTutoring, date, startTime, endTime);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> cancelSchedule(ScheduleDTO scheduleDTO) {
        // Check whether the request user actually has this tutoring.
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(scheduleDTO.getTutoringId(), tutorId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }
        Tutoring targetTutoring = tutoringOptional.get();

        // check if the cancellation is about regular schedule of the tutoring
        List<Time> timeList = targetTutoring.getTimes();
        LocalDate date = LocalDate.parse(scheduleDTO.getDate(), DateTimeFormatter.ISO_DATE);
        DayOfWeek day = date.getDayOfWeek();
        LocalTime startTime = LocalTime.parse(scheduleDTO.getStartTime(), DateTimeFormatter.ISO_LOCAL_TIME);
        for (Time time: timeList) {
            if (day.equals(time.getDay()) && startTime.equals(time.getStartTime())) {
                Optional<Cancellation> cancellationOptional = cancellationRepository.findByTutorIdAndCancelledDateTime(tutorId, LocalDateTime.of(date, startTime));
                if (cancellationOptional.isEmpty()) {
                    Cancellation newCancellation = Cancellation.builder().cancelledDateTime(LocalDateTime.of(date, startTime)).tutoring(targetTutoring).tutorId(tutorId).build();
                    cancellationRepository.save(newCancellation);
                    pushService.cancelScheduleNotification(targetTutoring, date, startTime);
                    return ResponseEntity.ok().build();
                }
            }
        }

        // ... cancellation about irregular schedule of the tutoring
        List<Irregular> irregularList = irregularRepository.findAllByTutorId(tutorId);
        for (Irregular irregular: irregularList) {
            if (date.isEqual(irregular.getDate()) && startTime.equals(irregular.getStartTime())) {
                irregularRepository.delete(irregular);
                pushService.cancelScheduleNotification(targetTutoring, date, startTime);
                return ResponseEntity.ok().build();
            }
        }

        // if there is no corresponding schedule
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_SCHEDULE.getMsg()));
        /* 정규일정의 요일, 시작시간과 받은 일정 날짜의 요일, 시작시간이 겹치면 그 일정을 취소함. -> cancellation 생성
           유저가 정규일정을 취소하려 했고, 성공했는데, 또 호출하여 복수의 취소가 생길 수 있음.
           유저가 정규일정을 이미 취소 후, 비정규일정을 만들고, 비정규일정을 취소하려 했는데, 또 정규일정 취소로 될 수 있음.
           따라서, 이미 존재하는 취소가 없을 시에만 정규일정 취소로 생각하고, 취소를 저장함. 이미 존재하면 비정규일정의 확인으로 넘어가게 됨.
           비정규일정의 날짜, 시작시간과 받은 일정 날짜, 시작시간이 겹치면 그 일정을 취소함. -> irregular 삭제
           위의 어느 경우에도 해당이 안되면 오류 반환.
         */
    }
    public ResponseEntity<?> changeRegularSchedule(ChangeRegularDTO changeRegularDTO) {
        // Check whether the request user actually has this tutoring.
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(changeRegularDTO.getTutoringId(), tutorId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }
        Tutoring targetTutoring = tutoringOptional.get();

        // compare day, startTime, endTime of new regular schedule with the other regular schedules
        // to check if it is preoccupied or not.
        List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(tutorId);
        List<Time> timeList = parseDayTimeString(changeRegularDTO.getDayTime(), targetTutoring);
        for (Tutoring tutoring : tutoringList) {
            if (tutoring.getId() == targetTutoring.getId()) continue;
            // if the dayTime is preoccupied by a certain regular time, return that time.
            for (Time time : timeList) {
                Time conflictTime = isPreoccupiedDayTime(tutoring, time);
                if (conflictTime != null) {
                    String tuteeName = "";
                    if (tutoring.getTuteeId()!=null && userRepository.findById(tutoring.getTuteeId()).isPresent()) {
                        tuteeName = userRepository.findById(tutoring.getTuteeId()).get().getName();
                    }
                    String subjectName = tutoring.getSubject().getName();
                    // 아래 요일 정보 보내주기 고민
                    String errorMessage = tuteeName + " 학생 " + subjectName + " 수업과 겹칩니다. (" + time.getStartTime().toString() + "~" + time.getEndTime().toString() + ")";
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(errorMessage));
                }
            }
        }

        // check all irregular times
        List<Irregular> irregularList = irregularRepository.findAllByTutorId(tutorId);
        LocalDateTime now = LocalDateTime.now();
        for (Irregular irregular: irregularList) {
            if (LocalDateTime.of(irregular.getDate(), irregular.getEndTime()).isBefore(now)) continue;
            for (Time time: timeList) {
                // 미래의 일정이고, 요일이 같고 시간이 겹치면
                if (time.getDay().equals(irregular.getDate().getDayOfWeek()) &&
                        isOverlapped(time.getStartTime(), time.getEndTime(), irregular.getStartTime(), irregular.getEndTime()))
                {
                    String tuteeName = "";
                    Tutoring overlappedTutoring = irregular.getTutoring();
                    if (overlappedTutoring.getTuteeId()!=null && userRepository.findById(overlappedTutoring.getTuteeId()).isPresent()) {
                        tuteeName = userRepository.findById(overlappedTutoring.getTuteeId()).get().getName();
                    }
                    String subjectName = overlappedTutoring.getSubject().getName();
                    String errorMessage = tuteeName + " 학생 " + subjectName + " 비정기 수업과 겹칩니다. 해당 일정 삭제 후 변경 가능 (" + irregular.getDate().toString() + " " + irregular.getStartTime().toString() + "~" + irregular.getEndTime().toString() + ")";
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
                }
            }
        }

        // delete all unnecessary cancellations of the tutoring (except cancellation of the same time)
        List<Cancellation> cancellationList = cancellationRepository.findAllByTutoring(targetTutoring);
        for (Cancellation cancellation: cancellationList) {
            LocalDateTime cancelledDateTime = cancellation.getCancelledDateTime();
            for (Time time: timeList) {
                if (cancelledDateTime.getDayOfWeek().equals(time.getDay()) &&
                    LocalTime.of(cancelledDateTime.getHour(), cancelledDateTime.getMinute()).equals(time.getStartTime())) {
                    continue;
                }
                cancellationRepository.delete(cancellation);
            }
        }
        timeRepository.deleteByTutoringId(targetTutoring.getId());
        timeRepository.saveAllAndFlush(timeList);
        pushService.changeRegularScheduleNotification(targetTutoring, changeRegularDTO.getDayTime());
        return ResponseEntity.ok().build();
        /* 에러 발생 부분, 서로 연관관계가 있는 entity의 경우 같은 transaction 안에서 삭제하게 되면 존재하지 않는 entity를 한 쪽이 갖게 됨.
           그래서 내부적으로 동기화가 강제적으로 진행돼서, 변경되지 않는 것. ->
        *  timeRepository.deleteAll(targetTutoring.getTimes());
           1. 쿼리로 해결 2. relation 제거 3. transaction 분리.
           2로 안돼서 1로 해결하였음.
        * */

        /* 다른 수업의 정규일정과 겹치면 에러응답 및 어떤 일정이 겹쳤는지 메시지 전송
           모든 비정규일정과 겹치면 에러응답 및 어떤 일정이 겹쳤는지 메시지 전송
           다만, 지나간 비정규일정을 고려하면 안된다.
           해당 수업의 정규취소는 모두 삭제한다. (단, 바뀐 요일과 바뀐 시간에 겹치는 정규취소는 유지한다.)
           이전의 정규시간을 모두 삭제하고, 새로운 정규시간을 등록한다.
          */
    }

    public ResponseEntity<?> registerRegularSchedule(Tutoring targetTutoring, String dayTime) {
        // called by registerTutoring
        // compare day, startTime, endTime of new regular schedule with the other regular schedules
        // to check if it is preoccupied or not.
        List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(targetTutoring.getTutorId());
        List<Time> timeList = parseDayTimeString(dayTime, targetTutoring);
        for (Tutoring tutoring : tutoringList) {
            if (tutoring.getId() == targetTutoring.getId()) continue;
            // if the dayTime is preoccupied by a certain regular time, return that time.
            for (Time time : timeList) {
                Time conflictTime = isPreoccupiedDayTime(tutoring, time);
                if (conflictTime != null) {
                    String tuteeName = "";
                    if (tutoring.getTuteeId()!=null && userRepository.findById(tutoring.getTuteeId()).isPresent()) {
                        tuteeName = userRepository.findById(tutoring.getTuteeId()).get().getName();
                    }
                    String subjectName = tutoring.getSubject().getName();
                    // 아래 요일 정보 보내주기 고민
                    String errorMessage = tuteeName + " 학생 " + subjectName + " 수업과 겹칩니다. (" + time.getStartTime().toString() + "~" + time.getEndTime().toString() + ")";
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(errorMessage));
                }
            }
        }
        // check all irregular times
        List<Irregular> irregularList = irregularRepository.findAllByTutorId(targetTutoring.getTutorId());
        LocalDateTime now = LocalDateTime.now();
        for (Irregular irregular: irregularList) {
            if (LocalDateTime.of(irregular.getDate(), irregular.getEndTime()).isBefore(now)) continue;
            for (Time time: timeList) {
                // 미래의 일정이고, 요일이 같고 시간이 겹치면
                if (time.getDay().equals(irregular.getDate().getDayOfWeek()) &&
                        isOverlapped(time.getStartTime(), time.getEndTime(), irregular.getStartTime(), irregular.getEndTime()))
                {
                    String tuteeName = "";
                    Tutoring overlappedTutoring = irregular.getTutoring();
                    if (overlappedTutoring.getTuteeId()!=null && userRepository.findById(overlappedTutoring.getTuteeId()).isPresent()) {
                        tuteeName = userRepository.findById(overlappedTutoring.getTuteeId()).get().getName();
                    }
                    String subjectName = overlappedTutoring.getSubject().getName();
                    String errorMessage = tuteeName + " 학생 " + subjectName + " 비정기 수업과 겹칩니다. 해당 일정 삭제 후 변경 가능 (" + irregular.getDate().toString() + " " + irregular.getStartTime().toString() + "~" + irregular.getEndTime().toString() + ")";
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
                }
            }
        }
        // register regular schedule
        timeRepository.saveAllAndFlush(timeList);
        return ResponseEntity.ok().build();
        /* 자신의 모든 정규일정을 확인하여 겹치면 어떤 일정이 겹쳤는지 메시지 전송
           모든 비정규일정과 겹치면 에러응답 및 어떤 일정이 겹쳤는지 전송
           다만, 지나간 비정규일정을 고려하면 안된다.
           정규시간을 등록한다.
          */
    }
    public ResponseEntity<?> getScheduleListYearMonth(Long tutoringId, int year, int month) {
        // Check whether the request user actually has this tutoring.
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(tutoringId, tutorId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }
        Tutoring tutoring = tutoringOptional.get();

        // get LocalDateTime object
        // String[] yearMonth = getScheduleDTO.getYearMonth().split(" ");
        LocalDate targetDate = LocalDate.of(year, month, 1);

        // times of the tutoring, scheduleList for the response, cancellations of the month, irregular list of the month
        List<Time> timeList = tutoring.getTimes();
        List<ScheduleInfoResponseDTO> scheduleList = new ArrayList<>();
        List<Cancellation> cancelledList = cancellationRepository.findAllByTutoring(tutoring).stream().filter(c ->
                (c.getCancelledDateTime().getYear() == targetDate.getYear() &&
                        c.getCancelledDateTime().getMonth() == targetDate.getMonth())
        ).toList();
        List<Irregular> irregularList = irregularRepository.findAllByTutoring(tutoring).stream().filter(i ->
                        (i.getDate().getYear() == targetDate.getYear() &&
                                i.getDate().getMonth() == targetDate.getMonth())
        ).toList();

        // get regular schedules of the month (At first, compare targetDate with startDate)
        if (targetDate.isAfter(tutoring.getStartDate().minusMonths(1))) {
            for (int i=0; i<targetDate.lengthOfMonth(); i++) {
                if (targetDate.plusDays(i).isBefore(tutoring.getStartDate())) {
                    continue;
                }
                DayOfWeek day = targetDate.getDayOfWeek().plus(i);
                for (Time time: timeList) {
                    if (time.getDay().equals(day)) {
                        scheduleList.add(ScheduleInfoResponseDTO.builder()
                                .date(Integer.toString(i+1))
                                .startTime(time.getStartTime().toString())
                                .endTime(time.getEndTime().toString())
                                .build());
                    }
                }
            }
        }

        // get rid of cancelled schedules
        for (Cancellation c: cancelledList) {
            Iterator <ScheduleInfoResponseDTO> it = scheduleList.iterator();
            while(it.hasNext()) {
                ScheduleInfoResponseDTO s = it.next();
                if (c.getCancelledDateTime().getDayOfMonth() == Integer.parseInt(s.getDate()) &&
                        c.getCancelledDateTime().toLocalTime().toString().equals(s.getStartTime())) {
                    it.remove();
                }
            }
        }

        // add irregular schedules
        for (Irregular i: irregularList) {
            scheduleList.add(ScheduleInfoResponseDTO.builder()
                    .date(Integer.toString(i.getDate().getDayOfMonth()))
                    .startTime(i.getStartTime().toString())
                    .endTime(i.getEndTime().toString())
                    .build()
            );
        }
        return ResponseEntity.ok(scheduleList);
        /* 받은 연월에 대한 DateTime 객체를 만들고, 해당 달의 모든 일정을 넣는 것.
           정규취소를 제외한 모든 정규일정을 추가하고,
           비정규일정을 모두 넣어준다.
         */
        /*
            a code below can cause unexpected behavior
            for (ScheduleInfoResponseDTO s : scheduleList) {
                if ( c.getCancelledDateTime().getDayOfMonth() == Integer.parseInt(s.getDate()) &&
                        c.getCancelledDateTime().toLocalTime().toString().equals(s.getStartTime())) {
                    scheduleList.remove(s);
                }
            }
         */


    }

    private Time isPreoccupiedDateTime(Tutoring tutoring, LocalDate date, DayOfWeek day, LocalTime startTime, LocalTime endTime) {
        List<Time> timeList = tutoring.getTimes();
        for (Time time: timeList) {
            if (day.equals(time.getDay()) &&
                    isOverlapped(startTime, endTime, time.getStartTime(), time.getEndTime()) &&
                    cancellationRepository.findAllByCancelledDateTime(LocalDateTime.of(date, time.getStartTime())).isEmpty())
            { // 정규시간과 요일 및 시간이 겹치고, 그 날에 정규수업 취소가 되어있지 않은 경우에 겹치는 시간 반환.
                return time;
            }
        }
        return null;
    }
    private Time isPreoccupiedDayTime(Tutoring tutoring, Time targetTime) {
        List<Time> timeList = tutoring.getTimes();
        for (Time time: timeList) {
            if (targetTime.getDay().equals(time.getDay()) &&
                    isOverlapped(targetTime.getStartTime(), targetTime.getEndTime(), time.getStartTime(), time.getEndTime()))
            { // 정규시간과 요일 및 시간이 겹치는 경우에 겹치는 시간 반환.
                return time;
            }
        }
        return null;
    }

    private boolean isOverlapped(LocalTime startTime1, LocalTime endTime1, LocalTime startTime2, LocalTime endTime2) {
        // start time is in-between or end time is in-between.
        return (startTime1.isAfter(startTime2) && startTime2.isBefore(endTime2)) || (endTime1.isAfter(startTime2) && endTime1.isBefore(endTime2));
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
