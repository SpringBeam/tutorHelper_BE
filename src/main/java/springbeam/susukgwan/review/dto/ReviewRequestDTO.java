package springbeam.susukgwan.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class ReviewRequestDTO { /* Review 요청 DTO 모음 */

    @Getter
    @Setter
    public static class Create { /* Review 생성 */
        @NotNull(message = "수업 번호는 필수 입력 항목입니다.")
        private Long tutoringId;
        @NotBlank(message = "복습 내용은 필수 입력 항목이며 공백을 제외한 문자를 하나 이상 포함해야 합니다.")
        private String body;
        @NotNull(message = "태그 번호는 필수 입력 항목입니다.")
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
        @NotNull(message = "완료 여부는 필수 입력 항목입니다.")
        private Boolean isCompleted;
    }

    @Getter
    @Setter
    public static class ListRequest { /* Review 리스트 */
        @NotNull(message = "수업 번호는 필수 입력 항목입니다.")
        private Long tutoringId;
    }
}
