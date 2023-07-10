package springbeam.susukgwan.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.fcm.FCMToken;
import springbeam.susukgwan.fcm.FCMTokenRepository;
import springbeam.susukgwan.user.dto.SignUpSocialUserDTO;
import springbeam.susukgwan.user.dto.UpdateDTO;
import springbeam.susukgwan.user.dto.UserDetailDTO;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FCMTokenRepository fcmTokenRepository;

    public ResponseEntity<?> signUpSocialUser(SignUpSocialUserDTO signUpSocialUserDTO) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getRole() == Role.NONE) {
                // update role and name
                if (signUpSocialUserDTO.getRole().equals("tutor")) {
                    user.setRole(Role.TUTOR);
                } else if (signUpSocialUserDTO.getRole().equals("tutee")) {
                    user.setRole(Role.TUTEE);
                } else if (signUpSocialUserDTO.getRole().equals("parent")) {
                    user.setRole(Role.PARENT);
                } else {
                    return ResponseEntity.badRequest().build();
                }
                user.setName(signUpSocialUserDTO.getName());
                userRepository.save(user);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
    }
    public ResponseEntity<?> getUserDetail() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return ResponseEntity.ok().body(
                    UserDetailDTO.builder().role(user.getRole().getRole()).name(user.getName()).userId(user.getUserId()).build()
            );
        }
        else return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
    }
    public ResponseEntity<?> updateUser(UpdateDTO updateDTO) {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setName(updateDTO.getName());
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }
        else return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
    }
    public ResponseEntity<?> deleteUser() {
        String userIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = Long.parseLong(userIdStr);
        Optional<User> userOptional = userRepository.findById(userId);
        // fcm 관련 임시 추가 내용. (onetoone 맵핑 보류)
        Optional<FCMToken> fcmOptional = fcmTokenRepository.findByUserId(userId);
        if (fcmOptional.isPresent()) {
            fcmTokenRepository.delete(fcmOptional.get());
        }
        if (userOptional.isPresent()) {
            userRepository.delete(userOptional.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
    }
}
