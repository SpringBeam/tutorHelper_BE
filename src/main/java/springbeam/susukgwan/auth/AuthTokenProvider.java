package springbeam.susukgwan.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AuthTokenProvider {
    private final Key key;
    private static final String AUTHORITIES_KEY = "role";
    @Getter
    private final Long accessTokenExpiry = 1000 * 60L * 60L * 48L; // 6시간 -> 48시간
    @Getter
    private final Long refreshTokenExpiry = 1000 * 60L * 60L * 24L * 14L; // 14일

    public AuthTokenProvider(@Value("${jwt.secret.key}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public AuthToken createAccessToken(String id, String role) {
        Date now = new Date();
        return new AuthToken(id, role, new Date(now.getTime() + accessTokenExpiry) ,key);
    }

    public AuthToken createRefreshToken(String id, String role) {
        Date now = new Date();
        return new AuthToken(id, role, new Date(now.getTime() + refreshTokenExpiry) ,key);
    }

    public AuthToken convertToAuthToken(String token) {
        return new AuthToken(token, key);
    }

    public Authentication getAuthentication(AuthToken authToken) {
        if (authToken.validate()) {
            Claims claims = authToken.getTokenClaims();
            Collection <? extends GrantedAuthority> authorities =
                    Arrays.stream(new String[]{claims.get(AUTHORITIES_KEY).toString()})
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());  // 권한을 SimpleGrantedAutority로 맵핑
            log.debug("claims subject := [{}]", claims.getSubject());
            User principal = new User(claims.getSubject(), "", authorities);
            // UserDetailsService와 UserPrincipal과 관련됨.
            return new UsernamePasswordAuthenticationToken(principal, authToken, authorities);
        }
        else {
            throw new TokenValidFailedException();
        }
    }
}
