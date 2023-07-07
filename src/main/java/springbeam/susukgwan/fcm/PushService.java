package springbeam.susukgwan.fcm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.assignment.Assignment;
import springbeam.susukgwan.assignment.AssignmentRepository;
import springbeam.susukgwan.schedule.dto.ReplaceScheduleDTO;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.user.Role;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
public class PushService {
    @Autowired
    private FCMService fcmService;
    @Autowired
    private FCMTokenRepository fcmTokenRepository;
    @Autowired
    private PushRepository pushRepository;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private UserRepository userRepository;

    // Notify tutor that tutee or parent approves the invitation.
    public void approveInvitationNotification(Tutoring tutoring, User invitedUser) {
        String title = "초대 승인";
        String topic = "approve";
        String body = "";
        if (invitedUser.getRole().equals(Role.TUTEE)) {
            body = invitedUser.getName() + " 학생이 " + tutoring.getSubject().getName() + " 수업 참여 요청을 수락했습니다.";
        }
        else {
            body = invitedUser.getName() + " 님이 학부모로서 " + tutoring.getSubject().getName() + " 수업 참여 요청을 수락했습니다.";
        }
        Optional<FCMToken> fcmTokenOptional = fcmTokenRepository.findByUserId(tutoring.getTutorId());
        if (fcmTokenOptional.isPresent() && fcmTokenOptional.get().isAlarmOn()) {
            FCMToken fcmToken = fcmTokenOptional.get();
            PushRequest pushRequest = PushRequest.builder()
                    .token(fcmToken.getFcmToken()).title(title).topic(topic).body(body).build();
            sendPushNotificationToToken(pushRequest);
            Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutoring.getTutorId()).isRead(false).build();
            pushRepository.save(pushSave);
        }
    }
    // Notify tutee and parent 1. canceled schedule
    public void cancelScheduleNotification(Tutoring tutoring, LocalDate date, LocalTime startTime) {
        String title = "일정 취소";
        String topic = "cancellation";
        String body = tutoring.getSubject().getName() + " 수업의 " + date.toString() + " " + startTime.toString() + "에 시작하는 일정이 취소되었습니다.";
        PushRequest pushRequest = PushRequest.builder()
                .title(title).topic(topic).body(body).build();
        // send to tutee
        if (tutoring.getTuteeId()!=null) {
            Optional<FCMToken> tuteeTokenOptional = fcmTokenRepository.findByUserId(tutoring.getTuteeId());
            if (tuteeTokenOptional.isPresent() && tuteeTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(tuteeTokenOptional.get().getFcmToken());
                sendPushNotificationToToken(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutoring.getTuteeId()).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
        // send to parent
        if (tutoring.getParentId()!=null) {
            Optional<FCMToken> parentTokenOptional = fcmTokenRepository.findByUserId(tutoring.getParentId());
            if (parentTokenOptional.isPresent() && parentTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(parentTokenOptional.get().getFcmToken());
                sendPushNotificationByTopic(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutoring.getParentId()).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
    }
    // Notify tutee and parent 2. new irregular schedule
    public void newIrregularScheduleNotification(Tutoring tutoring, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String title = "새 수업 일정";
        String topic = "irregular";
        String body = tutoring.getSubject().getName() + " 수업에 새 일정 (" + date.toString() + " " + startTime.toString() + "~" + endTime.toString() + ")이 등록되었습니다.";
        PushRequest pushRequest = PushRequest.builder()
                .title(title).topic(topic).body(body).build();
        // send to tutee
        if (tutoring.getTuteeId()!=null) {
            Optional<FCMToken> tuteeTokenOptional = fcmTokenRepository.findByUserId(tutoring.getTuteeId());
            if (tuteeTokenOptional.isPresent() && tuteeTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(tuteeTokenOptional.get().getFcmToken());
                sendPushNotificationToToken(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutoring.getTuteeId()).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
        // send to parent
        if (tutoring.getParentId()!=null) {
            Optional<FCMToken> parentTokenOptional = fcmTokenRepository.findByUserId(tutoring.getParentId());
            if (parentTokenOptional.isPresent() && parentTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(parentTokenOptional.get().getFcmToken());
                sendPushNotificationByTopic(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutoring.getParentId()).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
    }
    public void replaceScheduleNotification(Tutoring tutoring, ReplaceScheduleDTO replaceScheduleDTO) {
        String title = "수업시간 변경";
        String topic = "replace";
        String body = tutoring.getSubject().getName() + " 수업의" + replaceScheduleDTO.getDate() + " " + replaceScheduleDTO.getStartTime()
                + " 일정이 " + replaceScheduleDTO.getDateWant() + " " + replaceScheduleDTO.getStartTimeWant() + "(으)로 변경되었습니다.";
        PushRequest pushRequest = PushRequest.builder()
                .title(title).topic(topic).body(body).build();
        // send to tutee
        if (tutoring.getTuteeId()!=null) {
            Optional<FCMToken> tuteeTokenOptional = fcmTokenRepository.findByUserId(tutoring.getTuteeId());
            if (tuteeTokenOptional.isPresent() && tuteeTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(tuteeTokenOptional.get().getFcmToken());
                sendPushNotificationToToken(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutoring.getTuteeId()).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
        // send to parent
        if (tutoring.getParentId()!=null) {
            Optional<FCMToken> parentTokenOptional = fcmTokenRepository.findByUserId(tutoring.getParentId());
            if (parentTokenOptional.isPresent() && parentTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(parentTokenOptional.get().getFcmToken());
                sendPushNotificationByTopic(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutoring.getParentId()).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
    }

    // Notify tutee, parent 3. changed regular schedule
    public void changeRegularScheduleNotification(Tutoring tutoring, String dayTime) {
        String title = "정규시간 변경";
        String topic = "change";
        String body = tutoring.getSubject().getName() + " 수업의 시간이 " + replaceDay(dayTime) + "(으)로 변경되었습니다.";
        PushRequest pushRequest = PushRequest.builder()
                .title(title).topic(topic).body(body).build();
        // send to tutee
        if (tutoring.getTuteeId()!=null) {
            Optional<FCMToken> tuteeTokenOptional = fcmTokenRepository.findByUserId(tutoring.getTuteeId());
            if (tuteeTokenOptional.isPresent() && tuteeTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(tuteeTokenOptional.get().getFcmToken());
                sendPushNotificationToToken(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutoring.getTuteeId()).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
        // send to parent
        if (tutoring.getParentId()!=null) {
            Optional<FCMToken> parentTokenOptional = fcmTokenRepository.findByUserId(tutoring.getParentId());
            if (parentTokenOptional.isPresent() && parentTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(parentTokenOptional.get().getFcmToken());
                sendPushNotificationByTopic(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutoring.getParentId()).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
    }

    /* 선생님한테 숙제인증 등록알림 보내기 */
    public void assignmentSubmitNotification (Assignment assignment, Long tuteeId) {
        String title = "숙제 인증";
        String topic = "submit";
        String body = "";

        Optional<User> tutee = userRepository.findById(tuteeId);
        if (tutee.isPresent()) {
            body = "'" + tutee.get().getName() + "' 학생이 '" + assignment.getNote().getTutoring().getSubject().getName() + "' 수업의 '" + assignment.getBody() + "' 숙제에 인증피드를 올렸습니다.";
        }

        log.info(body);

        PushRequest pushRequest = PushRequest.builder()
                .title(title).topic(topic).body(body).build();

        Long tutorId = assignmentRepository.GetTutorIdOfAssignment(assignment.getId());

        // send to tutor
        if (tutorId != null) {
            Optional<FCMToken> tutorTokenOptional = fcmTokenRepository.findByUserId(tutorId);
            if (tutorTokenOptional.isPresent() && tutorTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(tutorTokenOptional.get().getFcmToken());
                sendPushNotificationByTopic(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutorId).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
    }

    /* 학생에게 숙제 당일 마감 알림 보내기 */
    public void assignmentDeadlineNotification (Assignment assignment) {
        String title = "숙제 마감";
        String topic = "deadline";
        String body = "'" + assignment.getNote().getTutoring().getSubject().getName() + "' 수업의 '" + assignment.getBody() + "' 숙제 마감기한이 오늘(" + assignment.getEndDate() + ")까지입니다.";

        log.info(body);

        PushRequest pushRequest = PushRequest.builder()
                .title(title).topic(topic).body(body).build();

        Long tuteeId = assignmentRepository.GetTuteeIdOfAssignment(assignment.getId());

        // send to tutee
        if (tuteeId != null) {
            Optional<FCMToken> tuteeTokenOptional = fcmTokenRepository.findByUserId(tuteeId);
            if (tuteeTokenOptional.isPresent() && tuteeTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(tuteeTokenOptional.get().getFcmToken());
                sendPushNotificationByTopic(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tuteeId).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
    }

    /* 선생님한테 숙제 미완료 알림 보내기 */
    public void assignmentCompletedNotification (Assignment assignment) {
        String title = "숙제 미완료";
        String topic = "completed";
        String body = "";

        Long tuteeId = assignment.getNote().getTutoring().getTuteeId();
        if (tuteeId != null) {
            Optional<User> tutee = userRepository.findById(tuteeId);
            if (tutee.isPresent()) {
                body = "'" + tutee.get().getName() + "' 학생이 '" + assignment.getNote().getTutoring().getSubject().getName() + "' 수업의 '" + assignment.getBody() + "' 숙제를 완료하지 않았습니다.";
            }
        }

        log.info(body);

        PushRequest pushRequest = PushRequest.builder()
                .title(title).topic(topic).body(body).build();

        // send to tutor
        Long tutorId = assignmentRepository.GetTutorIdOfAssignment(assignment.getId());
        if (tutorId != null) {
            Optional<FCMToken> tutorTokenOptional = fcmTokenRepository.findByUserId(tutorId);
            if (tutorTokenOptional.isPresent() && tutorTokenOptional.get().isAlarmOn()) {
                pushRequest.setToken(tutorTokenOptional.get().getFcmToken());
                sendPushNotificationByTopic(pushRequest);
                Push pushSave = Push.builder().title(title).topic(topic).body(body).receiverId(tutorId).isRead(false).build();
                pushRepository.save(pushSave);
            }
        }
    }

    private String replaceDay(String dayTime) {
        StringBuilder replacedDayTime = new StringBuilder();
        String[] split = dayTime.split(",");
        Iterator<String> it = Arrays.stream(split).iterator();
        while (it.hasNext()) {
            String eachDayTime = it.next().strip();
            String day = eachDayTime.split(" ")[0];
            DayOfWeek dayOfWeek = DayOfWeek.of(Integer.parseInt(day));
            switch (dayOfWeek.getValue()) {
                case 1:
                    eachDayTime.replaceFirst("1", "월");
                    break;
                case 2:
                    eachDayTime.replaceFirst("2", "화");
                    break;
                case 3:
                    eachDayTime.replaceFirst("3", "수");
                    break;
                case 4:
                    eachDayTime.replaceFirst("4", "목");
                    break;
                case 5:
                    eachDayTime.replaceFirst("5", "금");
                    break;
                case 6:
                    eachDayTime.replaceFirst("6", "토");
                    break;
                case 7:
                    eachDayTime.replaceFirst("7", "일");
                    break;
            }
            replacedDayTime.append(eachDayTime);
            if (it.hasNext()) {
                replacedDayTime.append(", ");
            }
        }
        return replacedDayTime.toString();
    }

    /* Send a push to a specific token */
    public boolean sendPushNotificationToToken (PushRequest request) {
        try {
            fcmService.sendMessageToToken(request);
            return true;
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }
    /* Send a push based on the topic - unused now */
    public void sendPushNotificationByTopic(PushRequest request) {
        try {
            fcmService.sendMessageByTopic(request);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    /* Send a push with data - unused for now */
    public void sendPushNotificationWithData(PushRequest request) {
        try {
            fcmService.sendMessageWithData(getSamplePayloadData(), request);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    /* make sample data used for PushNotification */
    private Map<String, String> getSamplePayloadData() {
        Map <String, String> data = new HashMap<>();
        data.put("messageId", "msgid");
        data.put("text", "text");
        return data;
    }
}
