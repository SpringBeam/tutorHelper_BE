package springbeam.susukgwan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMsgList {
    INVALID_ACCESS_TOKEN("유효하지 않은 액세스 토큰입니다."),
    NOT_EXPIRED("액세스 토큰이 아직 만료되지 않았습니다."),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다."),
    ;

    private final String msg;
}
