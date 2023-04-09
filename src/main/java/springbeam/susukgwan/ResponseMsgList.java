package springbeam.susukgwan;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMsgList {
    INVALID_ACCESS_TOKEN("유효하지 않은 액세스 토큰입니다."),
    NOT_EXPIRED("액세스 토큰이 아직 만료되지 않았습니다."),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다."),
    NOT_EXIST_TUTORING("수업이 존재하지 않습니다."),
    NOT_EXIST_TAG("태그가 존재하지 않습니다."),
    NOT_EXIST_NOTE("수업일지가 존재하지 않습니다."),
    NOT_EXIST_REVIEW("복습항목이 존재하지 않습니다."),
    TAG_CONSTRAINTS("태그가 이미 10개 존재하거나 이미 존재하는 이름의 태그입니다."),
    UPDATE_NOTHING("수정할 내용이 없습니다.")
    ;

    private final String msg;
    public static final String notnullMessage = "필수 입력 항목입니다.";
    public static final String notblankMessage = "필수 입력 항목이며 공백을 제외한 문자를 하나 이상 포함해야 합니다.";
}
