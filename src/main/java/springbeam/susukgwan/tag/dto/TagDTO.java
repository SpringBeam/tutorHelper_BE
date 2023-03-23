package springbeam.susukgwan.tag.dto;

import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.review.dto.ReviewDTO;
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
    public static class RequestList {
        private Long tutoringId;
    }

    @Getter
    @Setter
    public static class ResponseTagList {
        private int count;
        private List<ReviewDTO.Response.ReviewTagDTO> tagList;
    }
}
