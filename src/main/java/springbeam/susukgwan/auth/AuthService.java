package springbeam.susukgwan.auth;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.auth.dto.LogInDTO;
import springbeam.susukgwan.auth.dto.LoginSuccessResponseDTO;
import springbeam.susukgwan.auth.dto.SignUpDTO;
import springbeam.susukgwan.auth.vo.TokenRefreshVO;
import springbeam.susukgwan.user.Provider;
import springbeam.susukgwan.user.Role;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import java.time.LocalDateTime;
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
    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public ResponseEntity<?> registerUser(SignUpDTO signUpDTO) {
        // 아이디 중복 확인
        if (userRepository.findByUserId(signUpDTO.getUserId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.USER_ALREADY_EXISTS.getMsg()));
        }
        // 기본정보 설정 (일반 로그인은 provider와 socialId가 다음과 같이 등록되어 구분 가능해짐.)
        User user = User.builder()
                .provider(Provider.NONE)
                .roleType(RoleType.USER)
                .socialId(Integer.toString(0))
                .name(signUpDTO.getName())
                .createdAt(LocalDateTime.now())
                .userId(signUpDTO.getUserId())
                .build();
        // 역할 설정
        if (signUpDTO.getRole().equals("tutor")) {
            user.setRole(Role.TUTOR);
        } else if (signUpDTO.getRole().equals("tutee")) {
            user.setRole(Role.TUTEE);
        } else if (signUpDTO.getRole().equals("parent")) {
            user.setRole(Role.PARENT);
        } else {
            return ResponseEntity.badRequest().build();
        }
        // 패스워드 설정
        String encodedPassword = passwordEncoder.encode(signUpDTO.getPassword());
        user.setPassword(encodedPassword);
        userRepository.saveAndFlush(user);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> logIn(LogInDTO logInDTO) {
        // userId and password verification
        Optional<User> userOptional = userRepository.findByUserId(logInDTO.getUserId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USERID_AND_PASSWORD.getMsg()));
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(logInDTO.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseMsg(ResponseMsgList.NO_SUCH_USERID_AND_PASSWORD.getMsg()));
        }

        // create access token
        AuthToken accessToken = authTokenProvider.createAccessToken(user.getId().toString(), user.getRoleType().getCode());
        // create refresh token and update if needed
        AuthToken refreshToken = authTokenProvider.createRefreshToken(user.getId().toString(), user.getRoleType().getCode());
        Optional<UserRefreshToken> optional = userRefreshTokenRepository.findByUserId(user.getId().toString());
        if (optional.isPresent()) {
            // if refresh token is already exists, replace it with the newer one.
            UserRefreshToken userRefreshToken = optional.get();
            userRefreshToken.setRefreshToken(refreshToken.getToken());
            userRefreshTokenRepository.save(userRefreshToken);
        }
        else {
            // register a new refresh token
            UserRefreshToken userRefreshToken = UserRefreshToken.builder().userId(user.getId().toString()).refreshToken(refreshToken.getToken()).build();
            userRefreshTokenRepository.save(userRefreshToken);
        }
        LoginSuccessResponseDTO loginSuccessResponseDTO = LoginSuccessResponseDTO.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .accessExpired(authTokenProvider.getAccessTokenExpiry())
                .isEnabled(true)
                .userId(user.getId())
                .build();
        return ResponseEntity.ok(loginSuccessResponseDTO);
    }
}
