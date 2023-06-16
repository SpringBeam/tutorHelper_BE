package springbeam.susukgwan.review.dto;

import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.review.Review;

@Getter
@Setter
public class ReviewResponseDTO { /* Review 응답 DTO */
    private Long id;
    private String body;
    private Boolean isCompleted;
    private Long noteId;
    private Long tagId;
    private String tagName;

    public ReviewResponseDTO(Review review) {
        this.id = review.getId();
        this.body = review.getBody();
        this.isCompleted = review.getIsCompleted();
        this.noteId = review.getNote().getId();
        this.tagId = review.getTag().getId();
        this.tagName = review.getTag().getName();
    }
}
