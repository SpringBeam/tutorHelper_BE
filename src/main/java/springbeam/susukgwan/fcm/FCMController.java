package springbeam.susukgwan.fcm;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.fcm.dto.SaveFCMTokenDTO;
import springbeam.susukgwan.fcm.dto.SetAlarmDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
public class FCMController {
    @Autowired
    private FCMService fcmService;

    @PostMapping("/token")
    public ResponseEntity saveFCMToken(@RequestBody SaveFCMTokenDTO saveFCMTokenDTO) {
        return fcmService.saveFCMToken(saveFCMTokenDTO.getFcmToken());
    }
    @GetMapping("/test")
    public ResponseEntity testFCMAlarm() {
        return fcmService.testFCMAlarm();
    }
    @PatchMapping("/alarm")
    public ResponseEntity setAlarmState(@RequestBody SetAlarmDTO setAlarmDTO) {
        return fcmService.setAlarmState(setAlarmDTO.getIsAlarmOn());
    }
    @GetMapping("/read")
    public ResponseEntity readAllAlarm() {
        return fcmService.readAllAlarm();
    }
    @GetMapping("/push/list")
    public ResponseEntity getPushList() {
        return fcmService.getPushList();
    }
    @GetMapping("/alarm")
    public ResponseEntity getAlarmState() {
        return fcmService.getAlarmState();
    }
    @GetMapping("/alarm/new")
    public ResponseEntity checkNewAlarm() {
        return fcmService.checkNewAlarm();
    }
}
