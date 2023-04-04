package springbeam.susukgwan.tutoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.schedule.Time;
import springbeam.susukgwan.schedule.TimeRepository;
import springbeam.susukgwan.subject.Subject;
import springbeam.susukgwan.subject.SubjectRepository;
import springbeam.susukgwan.tutoring.dto.*;
import springbeam.susukgwan.user.Role;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

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

    public ResponseEntity<?> registerTutoring (RegisterTutoringDTO registerTutoringDTO) {
        /* TODO 선생, 학생 및 과목 중복 확인 -> 중복 요청 시 BAD REQUEST 반환 (나중에) */
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);

        Subject subject = findSubject(registerTutoringDTO.getSubject(), tutorId); // subject 가져오기

        LocalDate startDate = LocalDate.parse(registerTutoringDTO.getStartDate()); // startDate parsing
        Tutoring newTutoring = Tutoring.builder().tutorId(tutorId).startDate(startDate)
                        .subject(subject) // 과목 매핑
                        .build();
        newTutoring = tutoringRepository.save(newTutoring);

        String dayTimeString = registerTutoringDTO.getDayTime();
        String[] split = dayTimeString.split(",");
        Iterator<String> it = Arrays.stream(split).iterator();
        while (it.hasNext()) {
            String[] each = it.next().strip().split(" ");
            DayOfWeek dayOfWeek = DayOfWeek.of(Integer.valueOf(each[0]));
            LocalTime startTime = LocalTime.parse(each[1]);
            LocalTime endTime = LocalTime.parse(each[2]);
            Time regularTime = Time.builder()
                    .day(dayOfWeek)
                    .startTime(startTime)
                    .endTime(endTime)
                    .tutoring(newTutoring)
                            .build();
            timeRepository.save(regularTime);
        }
        return ResponseEntity.ok().build();
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
        Optional<Subject> duplicateSubject = subjectRepository.findByNameAndUserId(subjectName, tutorId); // 입력한 과목명, 튜터아이디로 이미 등록된 과목인지 확인
        if (duplicateSubject.isEmpty()) { // 기존에 없었으면 과목 새로 만듦
            subject = Subject.builder()
                    .name(subjectName)
                    .userId(tutorId)
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
            if (oldCodeOpt.isPresent()) {
                invitationCodeRepository.delete(oldCodeOpt.get());
            }
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
        if (tutoring.getTuteeId() == userId) {
            tutoring.setTuteeId(null);
            tutoringRepository.save(tutoring);
            return ResponseEntity.ok().build();
        } else if (tutoring.getParentId() == userId) {
            tutoring.setParentId(null);
            tutoringRepository.save(tutoring);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
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
