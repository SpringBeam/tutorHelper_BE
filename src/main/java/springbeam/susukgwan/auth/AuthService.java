package springbeam.susukgwan.auth;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springbeam.susukgwan.auth.vo.TokenRefreshVO;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthTokenProvider authTokenProvider;
    @Autowired
    private UserRefreshTokenRepository userRefreshTokenRepository;

    /* TODO 응답 체계화 필요  */
    public TokenRefreshVO refreshToken(String accessToken, String refreshToken) {
        TokenRefreshVO tokenRefreshVO = TokenRefreshVO.builder().accessToken("").refreshToken("").code("FAIL").build();
        // accessToken validate
        AuthToken authToken = authTokenProvider.convertToAuthToken(accessToken);
        // 이하 에러 간소화, 필요 시 객체 생성, 코드 설정
        if (!authToken.validate()) {
            return tokenRefreshVO;
        }
        // check whether accessToken is expired.
        Claims claims = authToken.getExpiredTokenClaims();
        if (claims == null) {
            return tokenRefreshVO;
        }
        String userId = claims.getSubject();
        RoleType roleType = RoleType.of(claims.get("role", String.class));
        // token refresh
        AuthToken refreshAuthToken = authTokenProvider.convertToAuthToken(refreshToken);
        if (!refreshAuthToken.validate()) { // 이미 지난 refreshToken은 fail
            return tokenRefreshVO;
        }
        // refreshToken repository validation
        Optional<UserRefreshToken> optional = userRefreshTokenRepository.findByUserIdAndRefreshToken(userId, refreshToken);
        if (optional.isEmpty()) {
            return tokenRefreshVO;
        }
        UserRefreshToken userRefreshToken = optional.get();
        // generate accessToken and refreshToken(if necessary)
        Date now = new Date();
        AuthToken newAccessToken = authTokenProvider.createAccessToken(userId, roleType.getCode());
        tokenRefreshVO.setAccessToken(newAccessToken.getToken());
        tokenRefreshVO.setRefreshToken(refreshToken);

        Long leftValidTime = refreshAuthToken.getTokenClaims().getExpiration().getTime() - now.getTime();
        if (leftValidTime < 1000 * 60L * 60L * 24L * 3L) {
            // 3일 보다 작으면 refreshToken 새로 발급 및 저장.
            AuthToken newRefreshToken = authTokenProvider.createRefreshToken(userId, roleType.getCode());
            userRefreshToken.setRefreshToken(newRefreshToken.getToken());
            userRefreshTokenRepository.save(userRefreshToken);
            tokenRefreshVO.setRefreshToken(newRefreshToken.getToken());
        }
        return tokenRefreshVO;
    }
}
