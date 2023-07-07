package springbeam.susukgwan.tutoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.assignment.AssignmentService;
import springbeam.susukgwan.assignment.dto.AssignmentResponseDTO;
import springbeam.susukgwan.fcm.PushService;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.note.NoteService;
import springbeam.susukgwan.review.ReviewService;
import springbeam.susukgwan.review.dto.ReviewRequestDTO;
import springbeam.susukgwan.review.dto.ReviewResponseDTO;
import springbeam.susukgwan.schedule.ScheduleService;
import springbeam.susukgwan.schedule.Time;
import springbeam.susukgwan.schedule.TimeRepository;
import springbeam.susukgwan.schedule.dto.ChangeRegularDTO;
import springbeam.susukgwan.schedule.dto.GetScheduleDTO;
import springbeam.susukgwan.schedule.dto.ScheduleInfoResponseDTO;
import springbeam.susukgwan.subject.Subject;
import springbeam.susukgwan.subject.SubjectRepository;
import springbeam.susukgwan.tutoring.color.Color;
import springbeam.susukgwan.tutoring.color.ColorList;
import springbeam.susukgwan.tutoring.dto.*;
import springbeam.susukgwan.user.Role;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class TutoringService {
    @Autowired
    private TutoringRepository tutoringRepository;
    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private InvitationCodeRepository invitationCodeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private PushService pushService;

    public ResponseEntity<?> registerTutoring (RegisterTutoringDTO registerTutoringDTO) {
        /* TODO 선생, 학생 및 과목 중복 확인 -> 중복 요청 시 BAD REQUEST 반환 (나중에) */
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);

        Subject subject = findSubject(registerTutoringDTO.getSubject(), tutorId); // subject 가져오기

        LocalDate startDate = LocalDate.parse(registerTutoringDTO.getStartDate()); // startDate parsing
        Tutoring newTutoring = Tutoring.builder().tutorId(tutorId).startDate(startDate)
                        .subject(subject) // 과목 매핑
                        .build();
        ResponseEntity<?> response = scheduleService.checkRegularScheduleRegistration(newTutoring, registerTutoringDTO.getDayTimeList());
        if (response.getStatusCode() == HttpStatus.OK) {
            // 일정 등록이 가능한 경우
            tutoringRepository.save(newTutoring);
            List<Time> timeListToSave = scheduleService.convertToTimeList(newTutoring, registerTutoringDTO.getDayTimeList());
            timeRepository.saveAll(timeListToSave);
            return ResponseEntity.ok().build();
        }
        else return response;
    }

    public ResponseEntity<?> updateTutoring(Long tutoringId, UpdateTutoringDTO updateTutoringDTO) {
        /* TODO완료 현재 액세스토큰 확인하여 본인 수업인지 확인 (로그인 인증 구현 후) */
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(tutoringId, tutorId);

        if (tutoringOptional.isPresent()) {
            Tutoring tutoring = tutoringOptional.get();
            Subject subject = findSubject(updateTutoringDTO.getSubject(), tutorId);
            tutoring.setSubject(subject);
            tutoring.setStartDate(LocalDate.parse(updateTutoringDTO.getStartDate()));
            tutoringRepository.save(tutoring);
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<?> deleteTutoring(Long tutoringId) {
        /* TODO완료 **현재 액세스토큰 확인하여 본인 수업인지 확인 (로그인 인증 구현 후) */
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(tutoringId, tutorId);
        if (tutoringOptional.isPresent()) {
            tutoringRepository.delete(tutoringOptional.get());
            return ResponseEntity.ok().build();
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    /* Subject : 기존에 있는거면 그거 반환, 없으면 새로 저장해서 반환 */
    public Subject findSubject(String subjectName, Long tutorId) {
        Subject subject = new Subject();
        Optional<Subject> duplicateSubject = subjectRepository.findByNameAndTutorId(subjectName, tutorId); // 입력한 과목명, 튜터아이디로 이미 등록된 과목인지 확인
        if (duplicateSubject.isEmpty()) { // 기존에 없었으면 과목 새로 만듦
            subject = Subject.builder()
                    .name(subjectName)
                    .tutorId(tutorId)
                    .build();
            subjectRepository.save(subject); // subject 저장
        } else { // 기존에 있었으면 있던거 가져옴
            subject = duplicateSubject.get();
        }
        return subject;
    }
    public ResponseEntity<?> invite(Long tutoringId, Role role) {
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(tutoringId, tutorId);
        if (tutoringOptional.isPresent()) {
            Tutoring tutoring = tutoringOptional.get();
            // exception1. Tutoring has a tutee or a parent already.
            if (role == Role.TUTEE) {
                if (tutoring.getTuteeId() != null) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.TUTEE_ALREADY_EXISTS.getMsg()));
                }
            }
            else if (role == Role.PARENT) {
                if (tutoring.getParentId() != null) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.PARENT_ALREADY_EXISTS.getMsg()));
                }
            }
            // If an invitation code for the role is already issued, delete it.
            Optional<InvitationCode> oldCodeOpt = invitationCodeRepository.findByTutoringIdAndRole(tutoringId, role);
            oldCodeOpt.ifPresent(invitationCode -> invitationCodeRepository.delete(invitationCode));
            // generate new random code(not duplicated)
            String newCode;
            do {
                newCode = generateRandomAlphaNumericString();
            } while (invitationCodeRepository.findByCode(newCode).isEmpty());
            InvitationCode invitationCode = InvitationCode.builder()
                    .code(newCode).tutoringId(tutoring.getId()).role(role).build();
            invitationCodeRepository.saveAndFlush(invitationCode);
            InvitationCodeDTO invitationCodeDTO = InvitationCodeDTO.builder().invitationCode(newCode).build();
            return ResponseEntity.ok().body(invitationCodeDTO);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }
    public ResponseEntity<?> approveInvitation(InvitationCodeDTO invitationCodeDTO) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        User user = userOptional.get();
        if (user.getRole() == Role.NONE || user.getRole() == Role.TUTOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<InvitationCode> invitationCodeOptional = invitationCodeRepository.findByCode(invitationCodeDTO.getInvitationCode());
        if (invitationCodeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_INVITATION_CODE.getMsg()));
        }
        InvitationCode invitationCode = invitationCodeOptional.get();
        if (user.getRole() != invitationCode.getRole()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // save tutee or parent to tutoring entity
        Optional<Tutoring> tutoringOptional = tutoringRepository.findById(invitationCode.getTutoringId());
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Tutoring tutoring = tutoringOptional.get();
        if (invitationCode.getRole() == Role.TUTEE) {
            tutoring.setTuteeId(userId);
        }
        else {
            tutoring.setParentId(userId);
        }
        tutoringRepository.save(tutoring);
        pushService.approveInvitationNotification(tutoring, user);
        return ResponseEntity.ok().build();
    }
    public ResponseEntity<?> withdrawFromTutoring(Long tutoringId) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        User user = userOptional.get();
        if (user.getRole() == Role.NONE || user.getRole() == Role.TUTOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Tutoring> tutoringOptional = tutoringRepository.findById(tutoringId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }
        Tutoring tutoring = tutoringOptional.get();
        if (tutoring.getTuteeId().equals(userId)) {
            tutoring.setTuteeId(null);
            tutoringRepository.save(tutoring);
            return ResponseEntity.ok().build();
        } else if (tutoring.getParentId().equals(userId)) {
            tutoring.setParentId(null);
            tutoringRepository.save(tutoring);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    /* get tutoring list of a tutor or tutee */
    public ResponseEntity<?> getTutoringList() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        User user = userOptional.get();
        if (user.getRole() == Role.TUTOR) {
            List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(user.getId());
            List<TutoringInfoResponseDTO.Tutor> tutoringInfos = tutoringList.stream().map(t -> {
                TutoringInfoResponseDTO.Tutor DTOTutor = TutoringInfoResponseDTO.Tutor.builder()
                        .tutoringId(t.getId())
                        .subject(t.getSubject().getName())
                        .tuteeName("")
                        .dayTime(makeDayTimeString(t.getTimes()))
                        .build();
                // 튜터링에 학생이 연결되지 않은 경우에는 null이므로 주의해서 다뤄야 한다.
                if (t.getTuteeId()!=null && userRepository.findById(t.getTuteeId()).isPresent()) {
                    DTOTutor.setTuteeName(userRepository.findById(t.getTuteeId()).get().getName());
                }
                // 학생이 없으면 빈 문자열
                return DTOTutor;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(tutoringInfos);
        } else if (user.getRole() == Role.TUTEE) {
            List<Tutoring> tutoringList = tutoringRepository.findAllByTuteeId(user.getId());
            List<TutoringInfoResponseDTO.Tutee> tutoringInfos = tutoringList.stream().map(t -> {
                TutoringInfoResponseDTO.Tutee DTOTutee = TutoringInfoResponseDTO.Tutee.builder()
                        .tutoringId(t.getId())
                        .subject(t.getSubject().getName())
                        .tutorName("")
                        .dayTime(makeDayTimeString(t.getTimes()))
                        .build();
                if (userRepository.findById(t.getTutorId()).isPresent()) {
                    DTOTutee.setTutorName(userRepository.findById(t.getTutorId()).get().getName());
                }
                return DTOTutee;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(tutoringInfos);
        }
        else if (user.getRole() == Role.PARENT) {
            List<Tutoring> tutoringList = tutoringRepository.findAllByParentId(user.getId());
            List<TutoringInfoResponseDTO.Parent> tutoringInfos = tutoringList.stream().map(t -> {
                TutoringInfoResponseDTO.Parent DTOParent = TutoringInfoResponseDTO.Parent.builder()
                        .tutoringId(t.getId())
                        .subject(t.getSubject().getName())
                        .tutorName("")
                        .tuteeName("")
                        .dayTime(makeDayTimeString(t.getTimes()))
                        .build();
                if (userRepository.findById(t.getTutorId()).isPresent()) {
                    DTOParent.setTutorName(userRepository.findById(t.getTutorId()).get().getName());
                }
                // 튜터링에 학생이 연결되지 않은 경우에는 null이므로 주의해서 다뤄야 한다.
                if (t.getTuteeId()!=null && userRepository.findById(t.getTuteeId()).isPresent()) {
                    DTOParent.setTuteeName(userRepository.findById(t.getTuteeId()).get().getName());
                }
                return DTOParent;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(tutoringInfos);
        }
        else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    /* get all info for tutoring overview (basic info for tutoring, schedule of month, noteList, assignmentList, reviewList) */
    public ResponseEntity<?> getTutoringDetail(Long tutoringId, int year, int month) {
        // Check whether the request user actually has this tutoring.
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Optional<Tutoring> tutoringOptional = tutoringRepository.findById(tutoringId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }
        Tutoring tutoring = tutoringOptional.get();
        Role role = Role.NONE;
        if (userId.equals(tutoring.getTutorId())) role = Role.TUTOR;
        else if (userId.equals(tutoring.getTuteeId())) role = Role.TUTEE;
        else if (userId.equals(tutoring.getParentId())) role = Role.PARENT;
        else return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        // get basic info of tutoring
        TutoringDetailDTO tutoringDetailDTO = TutoringDetailDTO.builder()
                .tutoringId(tutoring.getId())
                        .subject(tutoring.getSubject().getName())
                .tuteeName("")
                .parentName("")
                .startDate(tutoring.getStartDate().toString())
                .dayTime(makeDayTimeString(tutoring.getTimes()))
                .color(0)
        .build();
        if (tutoring.getColor() != null) {
            if (role == Role.TUTOR) tutoringDetailDTO.setColor(tutoring.getColor().getTutorColor().getValue());
            else if (role == Role.TUTEE) tutoringDetailDTO.setColor(tutoring.getColor().getTuteeColor().getValue());
        }
        if (tutoring.getTuteeId()!=null && userRepository.findById(tutoring.getTuteeId()).isPresent()) {
            tutoringDetailDTO.setTuteeName(userRepository.findById(tutoring.getTuteeId()).get().getName());
        }
        if (tutoring.getParentId()!=null && userRepository.findById(tutoring.getParentId()).isPresent()) {
            tutoringDetailDTO.setParentName(userRepository.findById(tutoring.getParentId()).get().getName());
        }
        // get schedule list and include it to DTO
        List<ScheduleInfoResponseDTO> scheduleList = scheduleService.getOnlyScheduleListYearMonth(tutoring, year, month);

        tutoringDetailDTO.setScheduleList(scheduleList);

        // get review list and include it to DTO
        List<ReviewResponseDTO> reviewResponseDTOS = reviewService.reviewListForDetail(tutoring);
        if (reviewResponseDTOS.size() >2) {
            reviewResponseDTOS = reviewResponseDTOS.subList(0,2);
        }
        tutoringDetailDTO.setReviewList(reviewResponseDTOS);

        // get assignment list and set
        List<AssignmentResponseDTO> assignmentResponseDTOS = assignmentService.assignmentListForDetail(tutoring);
        tutoringDetailDTO.setAssignmentList(assignmentResponseDTOS);

        // get note list and set
        List<Note> noteList = noteService.noteListForDetail(tutoring, year, month);
        List<NoteSimpleInfoDTO> noteSimpleInfoDTOS = noteList.stream().map(n -> NoteSimpleInfoDTO.builder()
                .noteId(n.getId())
                .date(String.valueOf(n.getDateTime().getDayOfMonth()))
                .startTime(n.getDateTime().toLocalTime().toString())
                .build()).toList();
        tutoringDetailDTO.setNoteList(noteSimpleInfoDTOS);

        return ResponseEntity.ok(tutoringDetailDTO);
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
    private String generateRandomAlphaNumericString() {
        int leftLimit = 48; // '0'
        int rightLimit = 122; // 'z'
        int length = 8;
        Random random = new Random();
        String randomStr = random.ints(leftLimit, rightLimit)
                .filter(i -> (i<=57 || i>=65) && (i<=90 || i>=97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return randomStr;
    }

}
