package springbeam.susukgwan.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import springbeam.susukgwan.auth.*;
import springbeam.susukgwan.auth.info.KakaoOAuth2UserInfo;
import springbeam.susukgwan.user.Provider;
import springbeam.susukgwan.user.User;
import springbeam.susukgwan.user.UserRepository;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    private final AuthTokenProvider authTokenProvider;
    // private final AppProperties
    @Autowired
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    // private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request,response,authentication);
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
        }
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);   // 현재 request에 대한 응답을 targetUrl로 redirect
    }
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // String redirectUri = request.getParameter("redirect_uri"); for debug
        String redirectUri = "http://localhost:3030/oauth/redirect";
        if (redirectUri == null) {
            //TODO 미리 등록된 redirect uri만 가능하도록 연결 성공 이후 설정해야 함!
            throw new IllegalArgumentException("Please give authorized redirect_uri param for the authentication");
        }

        String targetUrl = redirectUri;


        OAuth2AuthenticationToken auth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();   // 인증된 principal 반환
        // KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(oidcUser.getAttributes());
        // OAuth2UserService? 인증 후 User 객체가 저장되었을 것. authentication 객체에는 생성된 principal이 적용되었음.
        // UserPrincipal(OAuth2User)의 getName을 통해 userId를 접근할 수 있을 것임.
        String userId = authentication.getName();
        // Optional<User> user = userRepository.findByProviderAndSocialId(Provider.KAKAO, userInfo.getId());

        Collection<? extends GrantedAuthority> authorities = oidcUser.getAuthorities();

        RoleType roleType = hasAuthority(authorities, RoleType.ADMIN.getCode())? RoleType.ADMIN : RoleType.USER;
        Date now = new Date();

        // accessToken 생성
        AuthToken accessToken = authTokenProvider.createAccessToken(
                userId,
                roleType.getCode()
        );

        // refreshToken 생성 및 DB 조회 후 저장 혹은 업데이트
        AuthToken refreshToken = authTokenProvider.createRefreshToken(
                userId,
                roleType.getCode()
        );
        Optional<UserRefreshToken> userRefreshTokenOptional = userRefreshTokenRepository.findByUserId(userId);
        if (userRefreshTokenOptional.isPresent()) {
            UserRefreshToken userRefreshToken = userRefreshTokenOptional.get();
            userRefreshToken.setRefreshToken(refreshToken.getToken());
            userRefreshTokenRepository.save(userRefreshToken);
        }
        else {
            UserRefreshToken userRefreshToken = UserRefreshToken.builder()
                    .userId(userId).refreshToken(refreshToken.getToken()).build();
            userRefreshTokenRepository.save(userRefreshToken);
        }
        // TODO 아래 보내는 내용 고려하기. 왜 필요한지? 없애면 안되는지?
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("accessToken", accessToken.getToken())
                .queryParam("refreshToken", refreshToken.getToken())
                .queryParam("accessExpired", authTokenProvider.getAccessTokenExpiry())
                // .queryParam("refreshExpired", authTokenProvider.getRefreshTokenExpiry())  보내기 고려.
                .queryParam("userId", userId)
                .build().toUriString();
    }
    private boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
        if (authorities == null) {
            return false;
        }
        for (GrantedAuthority grantedAuthority : authorities) {
            if (authority.equals(grantedAuthority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
