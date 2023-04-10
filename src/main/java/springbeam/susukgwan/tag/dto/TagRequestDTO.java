package springbeam.susukgwan.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class TagRequestDTO { /* Tag 요청 DTO 모음 */
    @Getter
    @Setter
    public static class Create { /* Tag 생성 */
        @NotNull
        private Long tutoringId;
        @NotBlank
        private String tagName;
    }

    @Getter
    @Setter
    public static class ListRequest { /* Tag 리스트 */
        @NotNull
        private Long tutoringId;
    }

    @Getter
    @Setter
    public static class Update { /* Tag 수정 */
        @NotBlank
        private String tagName;
    }
}
