package springbeam.susukgwan.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.S3Service;
import springbeam.susukgwan.fcm.FCMToken;
import springbeam.susukgwan.fcm.FCMTokenRepository;
import springbeam.susukgwan.user.dto.SignUpSocialUserDTO;
import springbeam.susukgwan.user.dto.UpdateDTO;
import springbeam.susukgwan.user.vo.UserDetailVO;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FCMTokenRepository fcmTokenRepository;
    @Autowired
    private final S3Service s3Service;

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
                    UserDetailVO.builder().role(user.getRole().getRole()).name(user.getName()).build()
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

    /* 프로필 사진 업로드 */
    public ResponseEntity<?> uploadProfile(MultipartFile multipartFile) throws IOException {
        // 업로드 파일 존재여부 확인
        if (multipartFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.NO_FILE.getMsg()));
        }

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOptional = userRepository.findById(Long.parseLong(userId));
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 기존 프사 삭제
            if (user.getProfileImg() != null) {
                s3Service.delete(user.getProfileImg());
            }
            // 새 프사 업로드
            String originalFilename = multipartFile.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = "profile/profileImg-" + userId + extension;
            s3Service.upload(multipartFile, fileName);
            user.setProfileImg(fileName);
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
    }

    /* 프로필 사진 삭제 (기본 이미지로 변경) */
    public ResponseEntity<?> deleteProfile () {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOptional = userRepository.findById(Long.parseLong(userId));
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getProfileImg() != null) {
                s3Service.delete(user.getProfileImg());
                user.setProfileImg(null);
                userRepository.save(user);
            }
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
    }

    /* 프로필 사진 가져오기 */
    public ResponseEntity<?> getProfile(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getProfileImg() != null) {
                String url = s3Service.getPublicURL(user.getProfileImg());
                return ResponseEntity.ok(url);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_PROFILE.getMsg()));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USER_IN_DB.getMsg()));
    }
}
