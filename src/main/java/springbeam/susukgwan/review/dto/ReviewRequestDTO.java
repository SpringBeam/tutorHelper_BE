package springbeam.susukgwan.review.dto;

import lombok.*;

public class ReviewRequestDTO { /* Review 요청 DTO 모음 */

    @Getter
    @Setter
    public static class Create { /* Review 생성 */
        private Long tutoringId;
        private String body;
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
        private Boolean isCompleted;
    }

    @Getter
    @Setter
    public static class ListRequest { /* Review 리스트 */
        private Long tutoringId;
    }
}
