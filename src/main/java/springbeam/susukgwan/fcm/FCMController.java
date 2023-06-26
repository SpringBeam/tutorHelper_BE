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
}
