package springbeam.susukgwan.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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
    @Autowired
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    @Autowired
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;


    /* 소셜 로그인 인증 후 */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request,response,authentication);
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
        }
        clearAuthenticationAttributes(request, response); // 소셜 authentication 정보를 지워주고 이후 토큰으로 필터 시 authentication을 설정함.
        getRedirectStrategy().sendRedirect(request, response, targetUrl);   // 현재 request에 대한 응답을 targetUrl로 redirect
    }
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri =
                CookieUtil.getCookie(request, httpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                        .map(Cookie::getValue);

        if (redirectUri.isEmpty()) {
            //TODO 미리 등록된 redirect uri만 가능하도록 연결 성공 이후 설정해야 함!
            throw new IllegalArgumentException("Please give redirect_uri param for the authentication");
        }
        String targetUrl = redirectUri.get();

        // 여기서 사용되는 authentication은 customOauth2UserService에서 가입/로그인 처리를 하고
        // 그 oauth2User(UserPrincipal)을 인증에 담아 반환한 것이라고 예상할 수 있다.
        // UserPrincipal(OAuth2User)의 getName을 통해 userId를 접근할 수 있을 것임. (직접 override한 메소드 getName)
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();   // 인증된 principal 반환
        // KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(oidcUser.getAttributes()); <- 여러 소셜 구현 시 참고
        // Optional<User> user = userRepository.findByProviderAndSocialId(Provider.KAKAO, userInfo.getId()); <- 이거 대신 authentication에 담긴 내용으로 해결하는 게 로직 상 빠를듯

        String userId = authentication.getName();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        boolean isEnabled = userDetails.isEnabled();  // 역할 설정 완료 여부(즉, 가입 완료 여부)
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
                .queryParam("isEnabled", isEnabled)
                // .queryParam("refreshExpired", authTokenProvider.getRefreshTokenExpiry())  보내기 고려.
                .queryParam("userId", userId)
                .build().toUriString();
    }
    // remove authenticationAttributes from request and clear AuthenticationRequestCookies
    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
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
