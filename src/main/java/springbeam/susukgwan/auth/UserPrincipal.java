package springbeam.susukgwan.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import springbeam.susukgwan.user.Provider;
import springbeam.susukgwan.user.Role;
import springbeam.susukgwan.user.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class UserPrincipal implements OAuth2User, UserDetails, OidcUser {
    /* 인증을 위한 유저 추상화 객체 */
    private final String userId;  // User의 id를 String으로 변환하여 저장.
    private final String password;
    private final Provider providerType;
    private final RoleType roleType;    // 서비스 내 관리자, 사용자 구분
    private final Role role;    // 서비스 내 선생, 학생, 학부모 구분
    private final Collection<GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    @Override
    public String getName() {
        return userId;
    }
    @Override
    public String getUsername() {
        return userId;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        if (this.role == Role.NONE) {
            return false;
        }
        return true;
    }
    @Override
    public Map<String, Object> getClaims() {
        return null;
    }
    @Override
    public OidcIdToken getIdToken() {
        return null;
    }
    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }
    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getId().toString(),
                "",
                user.getProvider(),
                RoleType.USER,
                user.getRole(),
                Collections.singletonList(new SimpleGrantedAuthority(RoleType.USER.getCode()))
        );
    }
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }
}
