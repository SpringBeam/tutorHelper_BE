package springbeam.susukgwan.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.auth.dto.LogInDTO;
import springbeam.susukgwan.auth.dto.RefreshDTO;
import springbeam.susukgwan.auth.dto.SignUpDTO;
import springbeam.susukgwan.auth.vo.TokenRefreshVO;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @PostMapping("/refresh")
    public ResponseEntity refreshToken(HttpServletRequest request, @RequestBody RefreshDTO refreshDTO) {
        String accessToken = JwtHeaderUtil.getAccessToken(request);
        return authService.refreshToken(accessToken, refreshDTO.getRefreshToken());
    }
    @PostMapping("/signup")
    public ResponseEntity signUp(@RequestBody SignUpDTO signUpDTO) {
        return authService.registerUser(signUpDTO);
    }
    @PostMapping("/login")
    public ResponseEntity logIn(@RequestBody LogInDTO logInDTO) {
        return authService.logIn(logInDTO);
    }
}
