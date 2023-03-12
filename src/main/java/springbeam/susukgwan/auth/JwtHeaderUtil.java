package springbeam.susukgwan.auth;


import jakarta.servlet.http.HttpServletRequest;

public class JwtHeaderUtil {
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    public static String getAccessToken(HttpServletRequest httpServletRequest) {
        String headerValue = httpServletRequest.getHeader(HEADER_AUTHORIZATION);

        if (headerValue == null) {
            return null;
        }
        if (headerValue.startsWith(TOKEN_PREFIX)) {
            return headerValue.substring(TOKEN_PREFIX.length());
        }

        return null;
    }
}
