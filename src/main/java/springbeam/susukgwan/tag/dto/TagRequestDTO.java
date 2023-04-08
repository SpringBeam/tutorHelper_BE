package springbeam.susukgwan.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


public class TagRequestDTO { /* Tag 요청 DTO 모음 */
    @Getter
    @Setter
    public static class Create { /* Tag 생성 */
        @NotNull(message = "수업 번호는 필수 입력 항목입니다.")
        private Long tutoringId;
        @NotBlank(message = "태그 이름은 필수 입력 항목이며 공백을 제외한 문자를 하나 이상 포함해야 합니다.")
        private String tagName;
    }

    @Getter
    @Setter
    public static class ListRequest { /* Tag 리스트 */
        @NotNull(message = "수업 번호는 필수 입력 항목입니다.")
        private Long tutoringId;
    }

    @Getter
    @Setter
    public static class Update { /* Tag 수정 */
        @NotBlank(message = "태그 이름은 필수 입력 항목이며 공백을 제외한 문자를 하나 이상 포함해야 합니다.")
        private String tagName;
    }
}
