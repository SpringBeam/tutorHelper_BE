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
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.note.NoteService;
import springbeam.susukgwan.schedule.dto.*;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;
import springbeam.susukgwan.tutoring.dto.DayTimeDTO;
import springbeam.susukgwan.tutoring.dto.NoteSimpleInfoDTO;
import springbeam.susukgwan.user.Role;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;
import springbeam.susukgwan.user.UserService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private RegularRecordRepository regularRecordRepository;
    @Autowired
    private NoteService noteService;
    @Autowired
    private UserService userService;
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
                String errorMessage = tuteeName + " 학생 " + subjectName + " 수업과 겹칩니다. ("+ parseDayToString(time.getDay()) + "요일 "  + time.getStartTime().toString() + "~" + time.getEndTime().toString() + ")";
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
        // 미래의 일일 때만 학생에게 알려야 함. 임의로 선생이 바꾸는 것을 모두 알리는 것은 문제가 있음.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        if (startDateTime.isAfter(now)) {
            pushService.newIrregularScheduleNotification(targetTutoring, date, startTime, endTime);
        }
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
        // 과거의 일정에 대한 취소 요청이면 과거정규기록이 적용되는 시간대일 수 있음. -> 존재하면 적용
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        if (startDateTime.isBefore(now)) {
            // 과거의 일정일 경우 현재정규시간, 과거정규기록 중 어느 것이 적용되는 시간대인지 확인
            // 적용시간대가 오래된 것이 앞에 오도록 정렬됨. ~OrderBy~Asc
            List<RegularRecord> regularRecords = regularRecordRepository.findAllByTutoringOrderByAppliedUntilAsc(targetTutoring);
            // 과거 마지막 정규기록 ~ 현재 정규시간인 경우 혹은 정규기록이 없는 경우에는 현재 정규시간이 적용됨.
            for (RegularRecord regularRecord: regularRecords) {
                if (regularRecord.getAppliedUntil().isAfter(startDateTime)) {
                    // 이 정규시기가 적용되던 기간에 시작한 경우이므로, 이 처음 매칭되는 정규타임을 적용해야 함.
                    timeList = parseDayTimeString(regularRecord.getDayTimeString(), targetTutoring);
                    break;
                }
            }
        }
        for (Time time: timeList) {
            if (day.equals(time.getDay()) && startTime.equals(time.getStartTime())) {
                Optional<Cancellation> cancellationOptional = cancellationRepository.findByTutorIdAndCancelledDateTime(tutorId, LocalDateTime.of(date, startTime));
                if (cancellationOptional.isEmpty()) {
                    Cancellation newCancellation = Cancellation.builder().cancelledDateTime(LocalDateTime.of(date, startTime)).tutoring(targetTutoring).tutorId(tutorId).build();
                    cancellationRepository.save(newCancellation);
                    // 미래 일정이 취소된 경우만 알림을 보냄.
                    if (startDateTime.isAfter(now)) {
                        pushService.cancelScheduleNotification(targetTutoring, date, startTime);
                    }
                    return ResponseEntity.ok().build();
                }
            }
        }

        // ... cancellation about irregular schedule of the tutoring
        List<Irregular> irregularList = irregularRepository.findAllByTutorId(tutorId);
        for (Irregular irregular: irregularList) {
            if (date.isEqual(irregular.getDate()) && startTime.equals(irregular.getStartTime())) {
                irregularRepository.delete(irregular);
                // 미래 일정이 취소된 경우만 알림을 보냄.
                if (startDateTime.isAfter(now)) {
                    pushService.cancelScheduleNotification(targetTutoring, date, startTime);
                }
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
    public ResponseEntity<?> replaceSchedule(ReplaceScheduleDTO replaceScheduleDTO) {
        // Check whether the request user actually has this tutoring.
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(replaceScheduleDTO.getTutoringId(), tutorId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }
        Tutoring targetTutoring = tutoringOptional.get();

        // check whether the wanted schedule is available or not

        // compare wanted day, startTime, endTime with regular schedule to check if it is preoccupied or not.
        List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(tutorId);
        LocalDate dateWant = LocalDate.parse(replaceScheduleDTO.getDateWant(), DateTimeFormatter.ISO_DATE);
        DayOfWeek dayWant = dateWant.getDayOfWeek();
        LocalTime startTimeWant = LocalTime.parse(replaceScheduleDTO.getStartTimeWant(), DateTimeFormatter.ISO_LOCAL_TIME);
        LocalTime endTimeWant = LocalTime.parse(replaceScheduleDTO.getEndTimeWant(), DateTimeFormatter.ISO_LOCAL_TIME);
        LocalTime startTime = LocalTime.parse(replaceScheduleDTO.getStartTime(), DateTimeFormatter.ISO_LOCAL_TIME);
        for (Tutoring tutoring: tutoringList) {

            // if the dateTime is preoccupied by a certain regular time, return that time.
            Time time = isPreoccupiedDateTime(tutoring, dateWant, dayWant, startTimeWant, endTimeWant);
            // if there is a preoccupied time, and it is not a time to replace with.
            if (time != null && !time.getStartTime().equals(startTime)) {
                String tuteeName = "";
                // get one's name if tutee is registered
                if (tutoring.getTuteeId()!=null && userRepository.findById(tutoring.getTuteeId()).isPresent()) {
                    Optional<User> tuteeOptional = userRepository.findById(tutoring.getTuteeId());
                    tuteeName = tuteeOptional.get().getName();
                }
                String subjectName = tutoring.getSubject().getName();
                String errorMessage = tuteeName + " 학생 " + subjectName + " 수업과 겹칩니다. ("+ parseDayToString(time.getDay()) + "요일 "  + time.getStartTime().toString() + "~" + time.getEndTime().toString() + ")";
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(errorMessage));
            }
        }
        // check all irregular times
        List<Irregular> irregularList = irregularRepository.findAllByTutorId(tutorId);
        for (Irregular irregular: irregularList) {
            // 변경 요청일정은 제외

            // if there is a preoccupied irregular schedule between those times,
            // and it is not an original irregular time to replace with
            if (dateWant.isEqual(irregular.getDate()) &&
                    isOverlapped(startTimeWant, endTimeWant, irregular.getStartTime(), irregular.getEndTime()) &&
                        !irregular.getStartTime().equals(startTime))
            {
                String tuteeName = "";
                Tutoring overlappedTutoring = irregular.getTutoring(); // tutoring 존재 x의 경우는 일단 무시 tutoring 삭제 시 정규취소, irregular 취소 모두 삭제되도록 해야 됨.
                // get one's name if tutee is registered
                if (overlappedTutoring.getTuteeId()!=null && userRepository.findById(overlappedTutoring.getTuteeId()).isPresent()) {
                    Optional <User> tuteeOptional = userRepository.findById(overlappedTutoring.getTuteeId());
                    tuteeName = tuteeOptional.get().getName();
                }
                String subjectName = overlappedTutoring.getSubject().getName();
                String errorMessage = tuteeName + " 학생 " + subjectName + " 수업과 겹칩니다. (" + dateWant.toString() + " " + irregular.getStartTime().toString() + "~" + irregular.getEndTime().toString() + ")";
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
            }
        }
        Irregular newIrregular = Irregular.builder().date(dateWant).startTime(startTimeWant).endTime(endTimeWant)
                .tutoring(targetTutoring).tutorId(tutorId).build();

        // find original schedule to cancel

        // 변경대상 날짜(date, startTime, ... dateWant 등과는 다름)가 정규시간인지 아닌지 확인하는 것
        // check if the cancellation is about regular schedule of the tutoring
        // ... and if it is, save new schedule and delete old one.
        List<Time> timeList = targetTutoring.getTimes();
        LocalDate date = LocalDate.parse(replaceScheduleDTO.getDate(), DateTimeFormatter.ISO_DATE);
        DayOfWeek day = date.getDayOfWeek();

        // 과거의 일정에 대한 취소 요청이면 과거정규기록이 적용되는 시간대일 수 있음. -> 존재하면 적용
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        // 알람 여부를 위한 flag 과거 -> 과거로 변경된 일정만 알림을 제외할 필요 있음.
        LocalDateTime startDateTimeWant = LocalDateTime.of(dateWant, startTimeWant);
        boolean noPushFlag = (startDateTime.isBefore(now) && startDateTimeWant.isBefore(now));
        if (startDateTime.isBefore(now)) {
            // 과거의 일정일 경우 현재정규시간, 과거정규기록 중 어느 것이 적용되는 시간대인지 확인
            // 적용시간대가 오래된 것이 앞에 오도록 정렬됨. ~OrderBy~Asc
            List<RegularRecord> regularRecords = regularRecordRepository.findAllByTutoringOrderByAppliedUntilAsc(targetTutoring);
            // 과거 마지막 정규기록 ~ 현재 정규시간인 경우 혹은 정규기록이 없는 경우에는 현재 정규시간이 적용됨.
            for (RegularRecord regularRecord: regularRecords) {
                if (regularRecord.getAppliedUntil().isAfter(startDateTime)) {
                    // 이 정규시기가 적용되던 기간에 시작한 경우이므로, 이 처음 매칭되는 정규타임을 적용해야 함.
                    timeList = parseDayTimeString(regularRecord.getDayTimeString(), targetTutoring);
                    break;
                }
            }
        }
        for (Time time: timeList) {
            if (day.equals(time.getDay()) && startTime.equals(time.getStartTime())) {
                Optional<Cancellation> cancellationOptional = cancellationRepository.findByTutorIdAndCancelledDateTime(tutorId, LocalDateTime.of(date, startTime));
                if (cancellationOptional.isEmpty()) {
                    Cancellation newCancellation = Cancellation.builder().cancelledDateTime(LocalDateTime.of(date, startTime)).tutoring(targetTutoring).tutorId(tutorId).build();
                    // now save irregular schedule and cancel original schedule
                    irregularRepository.save(newIrregular);
                    cancellationRepository.save(newCancellation);
                    if (!noPushFlag) pushService.replaceScheduleNotification(targetTutoring, replaceScheduleDTO);
                    return ResponseEntity.ok().build();
                }
            }
        }

        // cancellation about irregular schedule of the tutoring
        for (Irregular irregular: irregularList) {
            if (date.isEqual(irregular.getDate()) && startTime.equals(irregular.getStartTime())) {
                // now save irregular schedule and delete original irregular schedule
                irregularRepository.save(newIrregular);
                irregularRepository.delete(irregular);
                if (!noPushFlag) pushService.replaceScheduleNotification(targetTutoring, replaceScheduleDTO);
                return ResponseEntity.ok().build();
            }
        }

        // if there is no such schedule.
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_SCHEDULE.getMsg()));
    }

    /* 새 정규시간이 지금 이후로 다른 일정과 겹치지 않으면 변경. 다른 수업의 정규시간과 겹치는지 비교, 현재시간 이후의 모든 비정기일정과 겹치는지 비교 */
    public ResponseEntity<?> changeRegularSchedule(ChangeRegularDTO changeRegularDTO) {
        // Check whether the request user actually has this tutoring.
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(changeRegularDTO.getTutoringId(), tutorId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }
        if (changeRegularDTO.getDayTimeList().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Tutoring targetTutoring = tutoringOptional.get();

        // compare day, startTime, endTime of new regular schedule with the other regular schedules
        // to check if it is preoccupied or not.
        List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(tutorId);
        List<Time> timeList = convertToTimeList(targetTutoring, changeRegularDTO.getDayTimeList());
        // List<Time> timeList = parseDayTimeString(changeRegularDTO.getDayTime(), targetTutoring);
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
                    String errorMessage = tuteeName + " 학생 " + subjectName + " 수업과 겹칩니다. (" + parseDayToString(time.getDay()) + "요일 " + time.getStartTime().toString() + "~" + time.getEndTime().toString() + ")";
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

        // delete all unnecessary cancellations of the tutoring
        // 해당 수업의 미래(현재 시간 이후) 정규수업시간 중 취소가 등록되어 있었으면
        // 암묵적으로 필요없는 것으로 분류하고 미래취소 모두 삭제 후 일정을 일관적으로 나타내도록 함
        List<Cancellation> cancellationList = cancellationRepository.findAllByTutoring(targetTutoring);
        for (Cancellation cancellation: cancellationList) {
            LocalDateTime cancelledDateTime = cancellation.getCancelledDateTime();
            if (cancelledDateTime.isAfter(now)) {
                cancellationRepository.delete(cancellation);
            }
            /*
            for (Time time: timeList) {
                if (cancelledDateTime.getDayOfWeek().equals(time.getDay()) &&
                    LocalTime.of(cancelledDateTime.getHour(), cancelledDateTime.getMinute()).equals(time.getStartTime())) {
                    continue;
                }
                cancellationRepository.delete(cancellation);
            }
             */
        }
        // 지금까지의 정규시간 기록
        List<Time> previousTimeList = targetTutoring.getTimes();
        String previousDayTimeStr = makeDayTimeString(previousTimeList);
        RegularRecord regularRecord = RegularRecord.builder()
                .tutoring(targetTutoring)
                .dayTimeString(previousDayTimeStr)
                .appliedUntil(now)
                .build();
        regularRecordRepository.save(regularRecord);

        timeRepository.deleteByTutoringId(targetTutoring.getId());
        timeRepository.saveAllAndFlush(timeList);
        pushService.changeRegularScheduleNotification(targetTutoring, makeDayTimeString(timeList));
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



    /* 새로운 수업이 지금 이후로 다른 수업의 정규시간과 겹치는지 비교, 현재시간 이후의 모든 비정기일정과 겹치는지 비교 */
    /* 비정규일정도 겹치지 않도록 하기! -> 즉, 과외 시작시간, 현재시간 이후에 겹치면 불가능한 등록 && 현재시간 이전인데 과외시간이 겹치면 정규취소를 등록 */
    /* 즉, 과외 시작시간 이후로 과거의 비정규일정이 겹치면 등록 불가능하게 하기보다 그 시간에 정규를 취소 등록하면 된다. */
    // check 하고 안될 시 오류 responseEntity를 보내주는 이 함수 이후로 시간, 수업을 저장하게 됨.
    public ResponseEntity<?> checkRegularScheduleRegistration(Tutoring targetTutoring, List<DayTimeDTO> dayTimeList) {
        // called by registerTutoring
        // compare day, startTime, endTime of new regular schedule with the other regular schedules
        // to check if it is preoccupied or not.
        List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(targetTutoring.getTutorId());
        log.info(tutoringList.toString());
        if (dayTimeList.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List <Time> timeList = convertToTimeList(targetTutoring, dayTimeList);
        // List<Time> timeList = parseDayTimeString(dayTime, targetTutoring);
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
                    String errorMessage = tuteeName + " 학생 " + subjectName + " 수업과 겹칩니다. (" + parseDayToString(time.getDay()) + "요일 "  + time.getStartTime().toString() + "~" + time.getEndTime().toString() + ")";
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(errorMessage));
                }
            }
        }
        // check all irregular times
        List<Irregular> irregularList = irregularRepository.findAllByTutorId(targetTutoring.getTutorId());
        LocalDateTime startedAt = LocalDateTime.of(targetTutoring.getStartDate(), LocalTime.of(0,0));
        LocalDateTime now = LocalDateTime.now();

        List<Irregular> timeToCancelBeforeRegistration = new ArrayList<>();
        for (Irregular irregular: irregularList) {
            // 시작 날짜 이후(시작 포함)에 대해서만 체크함.
            if (LocalDateTime.of(irregular.getDate(), irregular.getEndTime()).isBefore(startedAt)) continue;
            for (Time time: timeList) {
                // 요일이 같고 시간이 겹치면
                if (time.getDay().equals(irregular.getDate().getDayOfWeek()) &&
                        isOverlapped(time.getStartTime(), time.getEndTime(), irregular.getStartTime(), irregular.getEndTime()))
                {
                    // 과거일 경우 무시하기 위해 삭제 모으기
                    if (LocalDateTime.of(irregular.getDate(), irregular.getEndTime()).isBefore(now)) {
                        timeToCancelBeforeRegistration.add(irregular);
                    }
                    // 미래일 경우 불가능한 적용이므로 해당 내용 전달.
                    else {
                        // 현재 시간 이후일 경우 겹쳐서 등록 X 현재시간 이전일 경우  정규취소 일정을 모아서 등록 취소
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
        }
        for (Irregular irregular: timeToCancelBeforeRegistration) {
            Cancellation cancellation = Cancellation.builder().cancelledDateTime(LocalDateTime.of(irregular.getDate(), irregular.getStartTime()))
                            .tutorId(targetTutoring.getTutorId())
                                    .tutoring(targetTutoring)
                                            .build();
            cancellationRepository.save(cancellation);
        }
        // 새로운 수업의 정규일정 등록가능
        return ResponseEntity.ok().build();
    }
    public ResponseEntity<?> getScheduleListYearMonth(Long tutoringId, int year, int month) {
        // Check whether the request user is the tutor/tutee of this tutoring. (and parent)
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Optional<Tutoring> tutoringOptional = tutoringRepository.findById(tutoringId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }
        Tutoring tutoring = tutoringOptional.get();
        if (!userId.equals(tutoring.getTutorId()) && !userId.equals(tutoring.getTuteeId()) && !userId.equals(tutoring.getParentId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        // get LocalDateTime object of the corresponding year and month
        LocalDate targetDate = LocalDate.of(year, month, 1);

        // times of the tutoring, scheduleList for the response, cancellations of the month, irregular list of the month
        // List<Time> timeList = tutoring.getTimes();
        List<Cancellation> cancelledList = cancellationRepository.findAllByTutoring(tutoring).stream().filter(c ->
                (c.getCancelledDateTime().getYear() == targetDate.getYear() &&
                        c.getCancelledDateTime().getMonth() == targetDate.getMonth())
        ).toList();
        List<Irregular> irregularList = irregularRepository.findAllByTutoring(tutoring).stream().filter(i ->
                        (i.getDate().getYear() == targetDate.getYear() &&
                                i.getDate().getMonth() == targetDate.getMonth())
        ).toList();
        List<ScheduleInfoResponseDTO> scheduleInfoResponseDTOList = getRegularScheduleListByYearMonth(tutoring, targetDate);
        // 이하 반환용 스케줄 리스트
        List<ScheduleInfoResponseDTO> scheduleList = new ArrayList<>(scheduleInfoResponseDTOList);


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
            scheduleList.add(
                    ScheduleInfoResponseDTO.builder()
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

    public ResponseEntity<?> getAllScheduleListYearMonth(int year, int month) {
        // get all request user's tutoring list
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        User requestUser = userOptional.get();
        List<Tutoring> tutoringList;
        if (requestUser.getRole() == Role.TUTOR) {
            tutoringList = tutoringRepository.findAllByTutorId(userId);
        }
        else if (requestUser.getRole() == Role.TUTEE) {
            tutoringList = tutoringRepository.findAllByTuteeId(userId);
        }
        else if (requestUser.getRole() == Role.PARENT) {
            tutoringList = tutoringRepository.findAllByParentId(userId);
        }
        else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (tutoringList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TUTORING.getMsg()));
        }

        List<AllScheduleInfoResponseDTO> allScheduleInfoResponseDTOList = new ArrayList<>();
        LocalDate targetDate = LocalDate.of(year, month, 1);
        // Build basic info + scheduleList + noteList
        for (Tutoring tutoring : tutoringList) {
            // set basic info
            AllScheduleInfoResponseDTO allScheduleInfoResponseDTO = AllScheduleInfoResponseDTO.builder()
                            .tutoringId(tutoring.getId())
                                    .subject(tutoring.getSubject().getName())
                                            .color(0)
                                                    .personName("")
                                                            .profileImageUrl("")
                                                                    .build();
            // tutor | tutee and parent
            if (requestUser.getRole() == Role.TUTOR) {
                // set tutee name, image if a tutee exists
                if (tutoring.getTuteeId() != null && userRepository.findById(tutoring.getTuteeId()).isPresent()) {
                    allScheduleInfoResponseDTO.setPersonName(userRepository.findById(tutoring.getTuteeId()).get().getName());
                    ResponseEntity response = userService.getProfile(tutoring.getTuteeId());
                    if (response.getStatusCode() == HttpStatus.OK)
                        allScheduleInfoResponseDTO.setProfileImageUrl((String) response.getBody());
                }
                // set tutor color if exists
                if (tutoring.getColor() != null) {
                    allScheduleInfoResponseDTO.setColor(tutoring.getColor().getTutorColor().getValue());
                }
            }
            else {
                if (userRepository.findById(tutoring.getTutorId()).isPresent()) {
                    allScheduleInfoResponseDTO.setPersonName(userRepository.findById(tutoring.getTutorId()).get().getName());
                    ResponseEntity response = userService.getProfile(tutoring.getTutorId());
                    if (response.getStatusCode() == HttpStatus.OK)
                        allScheduleInfoResponseDTO.setProfileImageUrl((String) response.getBody());
                }
                // set tutee color if exists
                if (tutoring.getColor() != null) {
                    allScheduleInfoResponseDTO.setColor(tutoring.getColor().getTuteeColor().getValue());
                }
            }

            // set scheduleList for this tutoring
            // times of the tutoring, scheduleList for the response, cancellations of the month, irregular list of the month
            List<Cancellation> cancelledList = cancellationRepository.findAllByTutoring(tutoring).stream().filter(c ->
                    (c.getCancelledDateTime().getYear() == targetDate.getYear() &&
                            c.getCancelledDateTime().getMonth() == targetDate.getMonth())
            ).toList();
            List<Irregular> irregularList = irregularRepository.findAllByTutoring(tutoring).stream().filter(i ->
                    (i.getDate().getYear() == targetDate.getYear() &&
                            i.getDate().getMonth() == targetDate.getMonth())
            ).toList();

            // get regular schedules of the month
            List<ScheduleInfoResponseDTO> scheduleInfoResponseDTOList = getRegularScheduleListByYearMonth(tutoring, targetDate);
            // 이하 반환용 스케줄 리스트
            List<ScheduleInfoResponseDTO> scheduleList = new ArrayList<>(scheduleInfoResponseDTOList);

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

            // add irregular schedules of the month
            for (Irregular i: irregularList) {
                scheduleList.add(
                        ScheduleInfoResponseDTO.builder()
                                .date(Integer.toString(i.getDate().getDayOfMonth()))
                                .startTime(i.getStartTime().toString())
                                .endTime(i.getEndTime().toString())
                                .build()
                );
            }
            allScheduleInfoResponseDTO.setScheduleList(scheduleList);

            // get note list and set
            List<Note> noteList = noteService.noteListForDetail(tutoring, year, month);
            List<NoteSimpleInfoDTO> noteSimpleInfoDTOS = noteList.stream().map(n -> NoteSimpleInfoDTO.builder()
                    .noteId(n.getId())
                    .date(String.valueOf(n.getTutoringTime().getDayOfMonth()))
                    .startTime(n.getTutoringTime().toLocalTime().toString())
                    .build()).toList();
            allScheduleInfoResponseDTO.setNoteList(noteSimpleInfoDTOS);
            // add item to list
            allScheduleInfoResponseDTOList.add(allScheduleInfoResponseDTO);
        }
        return ResponseEntity.ok(allScheduleInfoResponseDTOList);
    }

    public ResponseEntity<?> getAllScheduleListYearMonthDay(int year, int month, int day) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        User user = userOptional.get();
        LocalDate targetDate = LocalDate.of(year, month, day);
        String dateStr = Integer.toString(targetDate.getDayOfMonth());
        List<Tutoring> tutoringList;
        List<ScheduleListByDayDTO> responseList = new ArrayList<>();
        if (user.getRole() == Role.TUTOR) {
            tutoringList = tutoringRepository.findAllByTutorId(user.getId());
        }
        else if (user.getRole() == Role.TUTEE) {
            tutoringList = tutoringRepository.findAllByTuteeId(user.getId());
        }
        else if (user.getRole() == Role.PARENT) {
            tutoringList = tutoringRepository.findAllByParentId(user.getId());
        }
        else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        for (Tutoring tutoring : tutoringList) {
            // get all schedule list (it considers all regular, irregular, cancel schedules)
            List<ScheduleInfoResponseDTO> scheduleInfoResponseDTOList = getOnlyScheduleListYearMonth(tutoring, year, month);
            for (ScheduleInfoResponseDTO scheduleInfoResponseDTO: scheduleInfoResponseDTOList) {
                // append an item to list if its date is targetDate
                if (scheduleInfoResponseDTO.getDate().equals(dateStr)) {
                    ScheduleListByDayDTO scheduleListByDayDTO = ScheduleListByDayDTO.builder()
                            .tutoringId(tutoring.getId())
                            .subject(tutoring.getSubject().getName())
                            .personName("")
                            .profileImageUrl("")
                            .color(0)
                            .startTime(scheduleInfoResponseDTO.getStartTime())
                            .endTime(scheduleInfoResponseDTO.getEndTime())
                            .noteId(0L)
                            .build();
                    LocalDateTime tutoringTime = LocalDateTime.of(targetDate, LocalTime.parse(scheduleInfoResponseDTO.getStartTime()));
                    Optional<Note> noteOptional = noteService.noteByTutoringAndTutoringTime(tutoring, tutoringTime);
                    noteOptional.ifPresent(note -> scheduleListByDayDTO.setNoteId(note.getId()));
                    // tutor side setting
                    if (user.getRole() == Role.TUTOR) {
                        if (tutoring.getTuteeId()!=null && userRepository.findById(tutoring.getTuteeId()).isPresent()) {
                            scheduleListByDayDTO.setPersonName(userRepository.findById(tutoring.getTuteeId()).get().getName());
                            ResponseEntity response = userService.getProfile(tutoring.getTuteeId());
                            if (response.getStatusCode() == HttpStatus.OK)
                                scheduleListByDayDTO.setProfileImageUrl((String) response.getBody());
                        }
                        // set tutor color if exists
                        if (tutoring.getColor() != null) {
                            scheduleListByDayDTO.setColor(tutoring.getColor().getTutorColor().getValue());
                        }
                    }
                    // tutee, parent side setting
                    else {
                        if (userRepository.findById(tutoring.getTutorId()).isPresent()) {
                            scheduleListByDayDTO.setPersonName(userRepository.findById(tutoring.getTutorId()).get().getName());
                            ResponseEntity response = userService.getProfile(tutoring.getTutorId());
                            if (response.getStatusCode() == HttpStatus.OK)
                                scheduleListByDayDTO.setProfileImageUrl((String) response.getBody());
                        }
                        // set tutee color if exists
                        if (tutoring.getColor() != null) {
                            scheduleListByDayDTO.setColor(tutoring.getColor().getTuteeColor().getValue());
                        }
                    }
                    responseList.add(scheduleListByDayDTO);
                }
            }
        }
        return ResponseEntity.ok(responseList);
    }
    public ResponseEntity<?> getAllScheduleListTutoringYearMonthDay(Long tutoringId, int year, int month, int day) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        User user = userOptional.get();
        Optional<Tutoring> tutoringOptional = tutoringRepository.findById(tutoringId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }
        Tutoring tutoring = tutoringOptional.get();
        List<Long> users = new ArrayList<>();
        users.add(tutoring.getTutorId());
        users.add(tutoring.getTuteeId());
        users.add(tutoring.getParentId());
        if (!users.contains(userId)) { // 해당 수업의 선생님, 학생, 학부모만 접근 가능
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
        }
        LocalDate targetDate = LocalDate.of(year, month, day);
        String dateStr = Integer.toString(targetDate.getDayOfMonth());
        List<ScheduleInfoResponseDTO> scheduleInfoResponseDTOList = getOnlyScheduleListYearMonth(tutoring, year, month);
        List<ScheduleListByDayDTO> responseList = new ArrayList<>();
        for (ScheduleInfoResponseDTO scheduleInfoResponseDTO: scheduleInfoResponseDTOList) {
            // append an item to list if its date is targetDate
            if (scheduleInfoResponseDTO.getDate().equals(dateStr)) {
                ScheduleListByDayDTO scheduleListByDayDTO = ScheduleListByDayDTO.builder()
                        .tutoringId(tutoring.getId())
                        .subject(tutoring.getSubject().getName())
                        .personName("")
                        .profileImageUrl("")
                        .color(0)
                        .startTime(scheduleInfoResponseDTO.getStartTime())
                        .endTime(scheduleInfoResponseDTO.getEndTime())
                        .noteId(0L)
                        .build();
                LocalDateTime tutoringTime = LocalDateTime.of(targetDate, LocalTime.parse(scheduleInfoResponseDTO.getStartTime()));
                Optional<Note> noteOptional = noteService.noteByTutoringAndTutoringTime(tutoring, tutoringTime);
                noteOptional.ifPresent(note -> scheduleListByDayDTO.setNoteId(note.getId()));
                // tutor side setting
                if (user.getRole() == Role.TUTOR) {
                    if (tutoring.getTuteeId()!=null && userRepository.findById(tutoring.getTuteeId()).isPresent()) {
                        scheduleListByDayDTO.setPersonName(userRepository.findById(tutoring.getTuteeId()).get().getName());
                        ResponseEntity response = userService.getProfile(tutoring.getTuteeId());
                        if (response.getStatusCode() == HttpStatus.OK)
                            scheduleListByDayDTO.setProfileImageUrl((String) response.getBody());
                    }
                    // set tutor color if exists
                    if (tutoring.getColor() != null) {
                        scheduleListByDayDTO.setColor(tutoring.getColor().getTutorColor().getValue());
                    }
                }
                // tutee, parent side setting
                else {
                    if (userRepository.findById(tutoring.getTutorId()).isPresent()) {
                        scheduleListByDayDTO.setPersonName(userRepository.findById(tutoring.getTutorId()).get().getName());
                        ResponseEntity response = userService.getProfile(tutoring.getTutorId());
                        if (response.getStatusCode() == HttpStatus.OK)
                            scheduleListByDayDTO.setProfileImageUrl((String) response.getBody());
                    }
                    // set tutee color if exists
                    if (tutoring.getColor() != null) {
                        scheduleListByDayDTO.setColor(tutoring.getColor().getTuteeColor().getValue());
                    }
                }
                responseList.add(scheduleListByDayDTO);
            }
        }
        return ResponseEntity.ok(responseList);
    }
    public List<ScheduleInfoResponseDTO> getOnlyScheduleListYearMonth(Tutoring tutoring, int year, int month) {
        // get LocalDateTime object of the corresponding year and month
        LocalDate targetDate = LocalDate.of(year, month, 1);

        // scheduleList for the response, cancellations of the month, irregular list of the month
        List<Cancellation> cancelledList = cancellationRepository.findAllByTutoring(tutoring).stream().filter(c ->
                (c.getCancelledDateTime().getYear() == targetDate.getYear() &&
                        c.getCancelledDateTime().getMonth() == targetDate.getMonth())
        ).toList();
        List<Irregular> irregularList = irregularRepository.findAllByTutoring(tutoring).stream().filter(i ->
                (i.getDate().getYear() == targetDate.getYear() &&
                        i.getDate().getMonth() == targetDate.getMonth())
        ).toList();

        List<ScheduleInfoResponseDTO> scheduleInfoResponseDTOList = getRegularScheduleListByYearMonth(tutoring, targetDate);
        // 이하 반환용 스케줄 리스트
        List<ScheduleInfoResponseDTO> scheduleList = new ArrayList<>(scheduleInfoResponseDTOList);

        // get rid of cancelled schedules
        for (Cancellation c: cancelledList) {
            scheduleList.removeIf(s -> c.getCancelledDateTime().getDayOfMonth() == Integer.parseInt(s.getDate()) &&
                    c.getCancelledDateTime().toLocalTime().toString().equals(s.getStartTime()));
        }

        // add irregular schedules
        for (Irregular i: irregularList) {
            scheduleList.add(
                    ScheduleInfoResponseDTO.builder()
                            .date(Integer.toString(i.getDate().getDayOfMonth()))
                            .startTime(i.getStartTime().toString())
                            .endTime(i.getEndTime().toString())
                            .build()
            );
        }
        return scheduleList;
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

    /* 정규일정 기록들, 현재 정규일정을 이용해서 그 달의 정규일정을 보내줌. */
    public List<ScheduleInfoResponseDTO> getRegularScheduleListByYearMonth(Tutoring tutoring, LocalDate targetDate) {
        // get regular schedules of the month (At first, compare targetDate with startDate)
        // 시작일이 적어도 현재 조회하는 월에 있거나 그 이전달이어야 함.
        List<ScheduleInfoResponseDTO> scheduleList = new ArrayList<>();
        if (targetDate.isAfter(tutoring.getStartDate().minusMonths(1))) {
            // 조회 시작을 원하는 날짜(~/1, targetDate) 0시 0분보다 현재 시간(now)이 미래에 있을때 이전(과거) 정규시간 조회가 필요해짐.
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime targetDateTime = LocalDateTime.of(targetDate, LocalTime.of(0,0));
            // 마지막으로 체크한 일수와 시간을 기록하는 변수 둘.
            int i = 0;
            LocalDateTime lastRegularAppliedDateTime = targetDateTime;
            // 정규시간 모두 조회해서 넣기 (조회하는 달의 1일보다 지금이 나중인 경우에만 이러한 조회가 필요)
            if (now.isAfter(targetDateTime)) {
                // 정규기록 모두 조회
                List<RegularRecord> regularRecords = regularRecordRepository.findAllByTutoringOrderByAppliedUntilAsc(tutoring)
                        .stream()
                        .filter(r -> (r.getAppliedUntil().isAfter(targetDateTime)))
                        .toList();
                if (!regularRecords.isEmpty()) {
                    Iterator<RegularRecord> it = regularRecords.iterator();
                    while (true) {
                        RegularRecord regularRecord = it.next(); // 꺼내놓고
                        lastRegularAppliedDateTime = regularRecord.getAppliedUntil();   // 마지막 적용일자 저장.
                        // 일자에 대해 순회하면서 확인
                        for (; i<targetDate.lengthOfMonth(); i++) {
                            if (targetDate.plusDays(i).isBefore(tutoring.getStartDate())) {
                                continue;
                            }
                            // 해당날쩌의 0시 00분이 정규 적용시기보다 이전이어야 함. 아니면 다음 정규 확인
                            if (!targetDateTime.plusDays(i).isBefore(regularRecord.getAppliedUntil())) {
                                break;
                            }
                            // 정규기록의 정규일정과 요일이 같고 시작시기(날짜, 시간) 또한 적용시기(날짜, 시간)보다 이전이면 담기
                            List<Time> regularRecordTimeList = parseDayTimeString(regularRecord.getDayTimeString(), tutoring);
                            DayOfWeek day = targetDate.getDayOfWeek().plus(i);
                            for (Time time: regularRecordTimeList) {
                                if (time.getDay().equals(day)) {
                                    // 정규 일시
                                    LocalDateTime appliedUntil = regularRecord.getAppliedUntil();
                                    if (LocalDateTime.of(targetDate.plusDays(i), time.getStartTime()).isBefore(appliedUntil)) {
                                        scheduleList.add(ScheduleInfoResponseDTO.builder()
                                                .date(Integer.toString(i+1))
                                                .startTime(time.getStartTime().toString())
                                                .endTime(time.getEndTime().toString())
                                                .build());
                                    }
                                }
                            }
                        }
                        // 이번 정규기록 확인 끝, 마지막 확인 일자는 i-1일 이고 마지막 일시도 저장되었음.
                        // 한 날에 여러 개의 정규기록이 존재할 수 있고, 현 정규일정 확인에 들어가기 전에도 i-1부터 확인해야 한다.
                        i = i-1;
                        // 모든 정규일자를 확인했으면 끝내기
                        if (!it.hasNext()) {
                            break;
                        }
                    }
                }
            }
            List<Time> timeList = tutoring.getTimes();
            // i-1부터 확인을 시작해서 모두 확인하되,
            // 마지막 적용시간(해당 월 1일 00시 00분 혹은 해당 월 마지막 정규기록)보다 이후 수업일정인 경우만 추가해준다.
            for (; i<targetDate.lengthOfMonth(); i++) {
                if (targetDate.plusDays(i).isBefore(tutoring.getStartDate())) {
                    continue;
                }
                DayOfWeek day = targetDate.getDayOfWeek().plus(i);
                for (Time time: timeList) {
                    if (time.getDay().equals(day)) {
                        // 요일이 같고 날짜 및 시간이 현재 정규 적용시기에 맞아야 된다.
                        if (LocalDateTime.of(targetDate.plusDays(i), time.getStartTime()).isAfter(lastRegularAppliedDateTime)) {
                            scheduleList.add(ScheduleInfoResponseDTO.builder()
                                    .date(Integer.toString(i+1))
                                    .startTime(time.getStartTime().toString())
                                    .endTime(time.getEndTime().toString())
                                    .build());
                        }
                    }
                }
            }
        }
        return scheduleList;
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
    /* 정규일정의 등록과 변경에 사용*/
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

    private boolean isOverlapped(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        // true if the two regular times are overlapped
        return !(s1.isAfter(e2) || s2.isAfter(e1));
    }

    public List<Time> convertToTimeList(Tutoring tutoring, List<DayTimeDTO> dayTimeList) {
        List<Time> timeList = new ArrayList<>();
        for (DayTimeDTO dayTimeDTO: dayTimeList) {
            DayOfWeek dayOfWeek = DayOfWeek.of(dayTimeDTO.getDay());
            LocalTime startTime = LocalTime.parse(dayTimeDTO.getStartTime());
            LocalTime endTime = LocalTime.parse(dayTimeDTO.getEndTime());
            Time time = Time.builder().day(dayOfWeek).startTime(startTime).endTime(endTime).tutoring(tutoring).build();
            timeList.add(time);
        }
        return timeList;
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
    private String parseDayToString(DayOfWeek dayOfWeek) {
        String day="";
        switch (dayOfWeek) {
            case MONDAY -> day = "월";
            case TUESDAY -> day = "화";
            case WEDNESDAY -> day = "수";
            case THURSDAY -> day = "목";
            case FRIDAY -> day = "금";
            case SATURDAY -> day = "토";
            case SUNDAY -> day = "일";
        }
        return day;
    }
    private String makeDayTimeString(List<Time> timeList) {
        StringBuilder dayTimeScheduleBuilder = new StringBuilder();
        for (Time time: timeList) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            // e.g. 1 13:50 15:50, ...
            dayTimeScheduleBuilder.append(time.getDay().getValue()).append(" ").append(time.getStartTime().format(formatter)).append(" ").append(time.getEndTime().format(formatter)).append(", ");
        }
        String dayTimeSchedule = dayTimeScheduleBuilder.toString();
        int lastIdx = dayTimeSchedule.length() - 1;
        dayTimeSchedule = dayTimeSchedule.substring(0, lastIdx - 1);
        return dayTimeSchedule;
    }
}
