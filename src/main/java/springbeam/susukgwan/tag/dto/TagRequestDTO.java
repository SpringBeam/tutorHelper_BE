package springbeam.susukgwan.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.ResponseMsgList;


public class TagRequestDTO { /* Tag 요청 DTO 모음 */
    @Getter
    @Setter
    public static class Create { /* Tag 생성 */
        @NotNull(message = "수업번호는 " + ResponseMsgList.notnullMessage)
        private Long tutoringId;
        @NotBlank(message = "태그이름은 " + ResponseMsgList.notblankMessage)
        private String tagName;
    }

    @Getter
    @Setter
    public static class ListRequest { /* Tag 리스트 */
        @NotNull(message = "수업번호는 " + ResponseMsgList.notnullMessage)
        private Long tutoringId;
    }

    @Getter
    @Setter
    public static class Update { /* Tag 수정 */
        @NotBlank(message = "태그이름은 " + ResponseMsgList.notblankMessage)
        private String tagName;
    }
}
