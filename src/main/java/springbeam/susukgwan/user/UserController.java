package springbeam.susukgwan.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springbeam.susukgwan.user.dto.SignUpSocialUserDTO;
import springbeam.susukgwan.user.dto.UpdateDTO;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity signUp(@RequestBody SignUpSocialUserDTO signUpSocialUserDTO) {
        return userService.signUpSocialUser(signUpSocialUserDTO);
    }
    @GetMapping("/detail")
    public ResponseEntity getUserDetail() {
        return userService.getUserDetail();
    }
    @PutMapping("/update")
    public ResponseEntity update(@RequestBody UpdateDTO updateDTO) {
        return userService.updateUser(updateDTO);
    }
    @DeleteMapping("/withdraw")
    public ResponseEntity delete() {
        return userService.deleteUser();
    }

    @PostMapping("/profile")
    public ResponseEntity uploadProfile(@RequestParam("image") MultipartFile multipartFile) throws IOException {
        return userService.uploadProfile(multipartFile);
    }

    @DeleteMapping("/profile")
    public ResponseEntity deleteProfile() {
        return userService.deleteProfile();
    }

    @GetMapping("/profile")
    public ResponseEntity getProfile() {
        return userService.getProfile();
    }
}
