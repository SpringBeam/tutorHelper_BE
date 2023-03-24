package springbeam.susukgwan.tag.dto;

import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.tag.Tag;

import java.util.List;

public class TagResponseDTO { /* Tag 응답 DTO 모음 */

    @Getter
    @Setter
    public static class CountAndTagList { /* Tag 리스트 + 개수 */
        private int count;
        private List<TagResponseDTO.SingleTag> tagList;
    }

    @Getter
    @Setter
    public static class SingleTag { /* Tag 1개 */
        private Long id;
        private String name;
        public SingleTag(Tag tag) {
            this.id = tag.getId();
            this.name = tag.getName();
        }
    }
}
