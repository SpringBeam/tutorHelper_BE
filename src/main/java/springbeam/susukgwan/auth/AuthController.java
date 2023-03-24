package springbeam.susukgwan.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.auth.dto.RefreshDTO;
import springbeam.susukgwan.auth.vo.TokenRefreshVO;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @PostMapping("/refresh")
    public TokenRefreshVO refreshToken(HttpServletRequest request, @RequestBody RefreshDTO refreshDTO) {
        String accessToken = JwtHeaderUtil.getAccessToken(request);
        TokenRefreshVO tokenRefreshVO = authService.refreshToken(accessToken, refreshDTO.getRefreshToken());
        // TODO 응답코드, 응답 규격화하기
        return tokenRefreshVO;
    }
}
