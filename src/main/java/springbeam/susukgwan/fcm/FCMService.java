package springbeam.susukgwan.fcm;

import com.google.firebase.messaging.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.fcm.dto.PushListDTO;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class FCMService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    FCMTokenRepository fcmTokenRepository;
    @Autowired
    PushRepository pushRepository;

    public ResponseEntity<?> saveFCMToken(String fcmToken) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        Optional<FCMToken> fcmTokenOptional = fcmTokenRepository.findByUserId(userId);
        if (fcmTokenOptional.isEmpty()) {
            FCMToken newToken = FCMToken.builder().fcmToken(fcmToken).userId(userId).isAlarmOn(false).build();
            fcmTokenRepository.save(newToken);
        }
        else {
            FCMToken oldToken = fcmTokenOptional.get();
            oldToken.setFcmToken(fcmToken);
            fcmTokenRepository.save(oldToken);
        }
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> setAlarmState(String isAlarmOn) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        Optional<FCMToken> fcmTokenOptional = fcmTokenRepository.findByUserId(userId);
        if (fcmTokenOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_FCM_TOKEN_ISSUED.getMsg()));
        }
        FCMToken fcmToken = fcmTokenOptional.get();
        if (isAlarmOn.equals("ON")) {
            fcmToken.setAlarmOn(true);
            fcmTokenRepository.save(fcmToken);
            return ResponseEntity.ok().build();
        }
        else if (isAlarmOn.equals("OFF")) {
            fcmToken.setAlarmOn(false);
            fcmTokenRepository.save(fcmToken);
            return ResponseEntity.ok().build();
        }
        else return ResponseEntity.badRequest().build();
    }

    public ResponseEntity<?> readAllAlarm() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        // List<Push> pushList = pushRepository.findAllByReceiverId(userId);
        List<Push> pushList = pushRepository.findAllByReceiverIdAndIsRead(userId, false);
        for (Push push: pushList) {
            push.setRead(true);
            pushRepository.save(push);
        }
        return ResponseEntity.ok().build();
    }
    public ResponseEntity<?> getPushList() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
        }
        List<Push> pushList = pushRepository.findAllByReceiverId(userId);
        PushListDTO pushListDTO = PushListDTO.builder().pushList(pushList).build();
        return ResponseEntity.ok().body(pushListDTO);
    }

    public ResponseEntity<?> testFCMAlarm() {
        // Check whether the request user actually has this tutoring.
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(tutorIdStr);
        Optional<FCMToken> fcmTokenOptional = fcmTokenRepository.findByUserId(userId);
        if (fcmTokenOptional.isPresent()) {
            PushRequest pushRequest = PushRequest.builder().title("test").topic("test").body("학교 종이 쌩쌩쌩").token(fcmTokenOptional.get().getFcmToken()).build();
            try {
                sendMessageToToken(pushRequest);
                return ResponseEntity.ok().build();
            }
            catch (Exception e) {
                log.error(e.getMessage());
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
        }
        else return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_FCM_TOKEN_ISSUED.getMsg()));
    }

    /* Send a message with a token */
    public void sendMessageToToken(PushRequest request) throws InterruptedException, ExecutionException, CancellationException {
        Message message = getPreconfiguredMessageToToken(request);
        // gson for log print
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);

        String response = sendAndGetResponse(message);
        log.info("Sent message to token. Device token: " + request.getToken() + ", " + response + " msg: " + jsonOutput);
    }

    /* Send a message with a topic */
    public void sendMessageByTopic(PushRequest request) throws InterruptedException, ExecutionException, CancellationException {
        Message message = getPreconfiguredMessageByTopic(request);
        String response = sendAndGetResponse(message);
        log.info("Sent message without data. Topic: " + request.getTopic() + ", " + response);
    }

    /* Send a message with data and token */
    public void sendMessageWithData (Map <String, String> data, PushRequest request) throws InterruptedException, ExecutionException, CancellationException {
        Message message = getPreconfiguredMessageWithData(data, request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        String response = sendAndGetResponse(message);
        log.info("Sent message with data. Topic: " + request.getTopic() + ", " + response+ " msg: "+jsonOutput);
    }

    /* Send one message and get response */
    private String sendAndGetResponse (Message message) throws InterruptedException, ExecutionException, CancellationException {
        return FirebaseMessaging.getInstance().sendAsync(message).get(); // wait for message ID String completion (success)
    }

    /* Make message with token (to the specific user) */
    private Message getPreconfiguredMessageToToken (PushRequest request) {
        return getPreconfiguredMessageBuilder(request)
                .setToken(request.getToken())
                .build();
    }

    /* (Unused for now) Make message with topic. Send messages to all clients who are subscribing this topic.
       Client side manages subscription list. */
    private Message getPreconfiguredMessageByTopic (PushRequest request) {
        return getPreconfiguredMessageBuilder(request)
                .setTopic(request.getTopic())
                .build();
    }

    /* (Unused for now) Make message(preconfigured) with data and token (to the specific user)
       Client interprets data and illustrates them. */
    private Message getPreconfiguredMessageWithData (Map<String, String> data, PushRequest request) {
        return getPreconfiguredMessageBuilder(request)
                .putAllData(data)
                .setToken(request.getToken())
                .build();
    }

    /* Configure message builder with pre-configuration and return it. Set config and set notification body */
    private Message.Builder getPreconfiguredMessageBuilder(PushRequest request) {
        AndroidConfig androidConfig = getAndroidConfig(request.getTopic());
        ApnsConfig apnsConfig = getApnsConfig(request.getTopic());
        return Message.builder()
                .setAndroidConfig(androidConfig)
                .setApnsConfig(apnsConfig)
                .setNotification(Notification.builder().setTitle(request.getTitle()).setBody(request.getBody()).build());
    }

    /* Build AndroidConfig with TOPIC and return (set notification collapse key and tag). Use sound and color configured by NotificationParameter.
       ref: https://firebase.google.com/docs/cloud-messaging/concept-options */
    private AndroidConfig getAndroidConfig (String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofDays(14).toMillis())
                .setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.NORMAL)
                .setNotification(AndroidNotification.builder()
                        .setSound(NotificationParameter.SOUND.getValue())
                        .setColor(NotificationParameter.COLOR.getValue())
                        .setTag(topic)
                        .build())
                .build();
    }

    /* Build ApnsConfig with TOPIC and return (set notification type and identifier) */
    private ApnsConfig getApnsConfig (String topic) {
        return ApnsConfig.builder()
                .setAps(
                        Aps.builder()
                                .setCategory(topic)
                                .setThreadId(topic)
                                .build()
                ).build();
    }

}
