package springbeam.susukgwan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMsgList {
    INVALID_ACCESS_TOKEN("유효하지 않은 액세스 토큰입니다."),
    NOT_EXPIRED("액세스 토큰이 아직 만료되지 않았습니다."),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다."),
    TUTEE_ALREADY_EXISTS("학생이 이미 수업에 등록되었습니다."),
    PARENT_ALREADY_EXISTS("학부모가 이미 수업에 등록되었습니다."),
    NO_SUCH_USER_IN_DB("해당 유저의 정보가 DB에 존재하지 않습니다."),
    NO_SUCH_INVITATION_CODE("해당하는 초대 코드가 존재하지 않습니다."),
    NO_SUCH_TUTORING("해당 수업이 존재하지 않습니다.")
    ;

    private final String msg;
}
