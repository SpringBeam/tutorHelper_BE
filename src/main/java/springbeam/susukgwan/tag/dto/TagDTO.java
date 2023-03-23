package springbeam.susukgwan.tag.dto;

import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.tag.Tag;

import java.util.List;

public class TagDTO {
    @Getter
    @Setter
    public static class Create {
        private Long subjectId;
        private String tagName;
    }

    @Getter
    @Setter
    public static class ListRequest {
        private Long tutoringId;
    }

    @Getter
    @Setter
    public static class ResponseTagList {
        private int count;
        private List<ResponseTag> tagList;
    }

    @Getter
    @Setter
    public static class ResponseTag {
        private Long id;
        private String name;
        public ResponseTag(Tag tag) {
            this.id = tag.getId();
            this.name = tag.getName();
        }
    }
}
