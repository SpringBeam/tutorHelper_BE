package springbeam.susukgwan.auth;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
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

    public ResponseEntity<?> refreshToken(String accessToken, String refreshToken) {
        // accessToken validate
        AuthToken authToken = authTokenProvider.convertToAuthToken(accessToken);
        if (!authToken.validate()) {
            ResponseMsg message = new ResponseMsg(ResponseMsgList.INVALID_ACCESS_TOKEN.getMsg());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
        }
        // check whether accessToken is expired.
        Claims claims = authToken.getExpiredTokenClaims();
        if (claims == null) {
            ResponseMsg message = new ResponseMsg(ResponseMsgList.NOT_EXPIRED.getMsg());
            return ResponseEntity.badRequest().body(message);
        }
        String userId = claims.getSubject();
        RoleType roleType = RoleType.of(claims.get("role", String.class));
        // refreshToken validate
        AuthToken refreshAuthToken = authTokenProvider.convertToAuthToken(refreshToken);
        if (!refreshAuthToken.validate()) { // 이미 지난 refreshToken은 fail
            ResponseMsg message = new ResponseMsg(ResponseMsgList.INVALID_REFRESH_TOKEN.getMsg());
            return ResponseEntity.badRequest().body(message);
        }
        // refreshToken repository validation
        Optional<UserRefreshToken> optional = userRefreshTokenRepository.findByUserIdAndRefreshToken(userId, refreshToken);
        if (optional.isEmpty()) {
            ResponseMsg message = new ResponseMsg(ResponseMsgList.INVALID_REFRESH_TOKEN.getMsg());
            return ResponseEntity.badRequest().body(message);
        }
        UserRefreshToken userRefreshToken = optional.get();
        // generate accessToken
        Date now = new Date();
        AuthToken newAccessToken = authTokenProvider.createAccessToken(userId, roleType.getCode());
        TokenRefreshVO tokenRefreshVO = TokenRefreshVO.builder().accessToken(newAccessToken.getToken()).refreshToken(refreshToken).build();
        // and regenerate refreshToken(if necessary)
        Long leftValidTime = refreshAuthToken.getTokenClaims().getExpiration().getTime() - now.getTime();
        if (leftValidTime < 1000 * 60L * 60L * 24L * 3L) {
            // 3일보다 작으면 refreshToken 새로 발급 및 저장.
            AuthToken newRefreshToken = authTokenProvider.createRefreshToken(userId, roleType.getCode());
            userRefreshToken.setRefreshToken(newRefreshToken.getToken());
            userRefreshTokenRepository.save(userRefreshToken);
            tokenRefreshVO.setRefreshToken(newRefreshToken.getToken());
        }
        return ResponseEntity.ok(tokenRefreshVO);
    }
}
