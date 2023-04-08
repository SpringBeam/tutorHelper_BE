package springbeam.susukgwan.tag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


public class TagRequestDTO { /* Tag 요청 DTO 모음 */
    @Getter
    @Setter
    public static class Create { /* Tag 생성 */
        private Long tutoringId;
        @NotBlank
        private String tagName;
    }

    @Getter
    @Setter
    public static class ListRequest { /* Tag 리스트 */
        private Long tutoringId;
    }
}
