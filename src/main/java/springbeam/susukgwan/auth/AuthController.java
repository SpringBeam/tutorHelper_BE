package springbeam.susukgwan.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springbeam.susukgwan.auth.vo.TokenRefreshVO;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @GetMapping("/refresh")
    public TokenRefreshVO refreshToken(HttpServletRequest request, @RequestParam String refreshToken) {
        String accessToken = JwtHeaderUtil.getAccessToken(request);
        TokenRefreshVO tokenRefreshVO = authService.refreshToken(accessToken, refreshToken);
        return tokenRefreshVO;
    }
}
