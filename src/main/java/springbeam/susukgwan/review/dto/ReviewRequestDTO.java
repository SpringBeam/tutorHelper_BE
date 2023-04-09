package springbeam.susukgwan.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import springbeam.susukgwan.ResponseMsgList;

public class ReviewRequestDTO { /* Review 요청 DTO 모음 */

    @Getter
    @Setter
    public static class Create { /* Review 생성 */
        @NotNull(message = "수업번호는 " + ResponseMsgList.notnullMessage)
        private Long tutoringId;
        @NotBlank(message = "복습내용은 " + ResponseMsgList.notblankMessage)
        private String body;
        @NotNull(message = "태그번호는 " + ResponseMsgList.notnullMessage)
        private Long tagId;
    }

    @Getter
    @Setter
    public static class Update { /* Review 수정 */
        private String body;
        private Long tagId;
    }

    @Getter
    @Setter
    public static class Check { /* Review 완료여부 체크 */
        @NotNull(message = "완료여부는 " + ResponseMsgList.notnullMessage)
        private Boolean isCompleted;
    }

    @Getter
    @Setter
    public static class ListRequest { /* Review 리스트 */
        @NotNull(message = "수업번호는 " + ResponseMsgList.notnullMessage)
        private Long tutoringId;
    }
}
