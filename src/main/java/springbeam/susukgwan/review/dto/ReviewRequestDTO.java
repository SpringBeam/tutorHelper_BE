package springbeam.susukgwan.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

public class ReviewRequestDTO { /* Review 요청 DTO 모음 */

    @Getter
    @Setter
    public static class Create { /* Review 생성 */
        @NotNull
        private Long tutoringId;
        @NotBlank
        private String body;
        @NotNull
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
        @NotNull
        private Boolean isCompleted;
    }

    @Getter
    @Setter
    public static class ListRequest { /* Review 리스트 */
        @NotNull
        private Long tutoringId;
    }

    @Getter
    @Setter
    public static class MultiDelete { /* Review 여러개 삭제 */
        @NotNull
        private List<Long> reviewIdList;
    }
}
