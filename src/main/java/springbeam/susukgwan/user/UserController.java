package springbeam.susukgwan.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.user.dto.SignUpSocialUserDTO;
import springbeam.susukgwan.user.dto.UpdateDTO;

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
}
