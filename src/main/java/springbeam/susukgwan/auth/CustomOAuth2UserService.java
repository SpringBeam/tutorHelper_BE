package springbeam.susukgwan.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.auth.info.KakaoOAuth2UserInfo;
import springbeam.susukgwan.user.Provider;
import springbeam.susukgwan.user.Role;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Autowired
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            return this.process(oAuth2User);
        }
        /*
        catch (AuthenticationException e) {
            throw e;
        }
         */
        catch (Exception e) {
            e.printStackTrace();
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }

    private OAuth2User process(OAuth2User user) {
        KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(user.getAttributes());

        Optional<User> oldUser = userRepository.findByProviderAndSocialId(Provider.KAKAO, userInfo.getId()); // kakao & social id로 검색
        User principal;
        if (oldUser.isEmpty()) {
            principal = createUser(userInfo);
        }
        else {
            principal = oldUser.get();
        }
        return UserPrincipal.create(principal, user.getAttributes());
    }

    private User createUser(KakaoOAuth2UserInfo userInfo) {
        User user = User.builder()
                .provider(Provider.KAKAO)
                .roleType(RoleType.USER)
                .socialId(userInfo.getId())
                .name(userInfo.getName())
                .role(Role.NONE)    // 가입 이후 역할이 없으면 역할 설정 계속 호출되도록. 첫 가입 시 역할 설정.
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.saveAndFlush(user);
    }

}
