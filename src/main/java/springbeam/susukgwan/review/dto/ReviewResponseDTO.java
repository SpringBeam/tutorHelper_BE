package springbeam.susukgwan.review.dto;

import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.note.dto.NoteResponseDTO;
import springbeam.susukgwan.review.Review;
import springbeam.susukgwan.tag.dto.TagResponseDTO;

@Getter
@Setter
public class ReviewResponseDTO { /* Review 응답 DTO */
    private Long id;
    private String body;
    private Boolean isCompleted;
    private NoteResponseDTO note;
    private TagResponseDTO.SingleTag tag;

    public ReviewResponseDTO(Review review) {
        this.id = review.getId();
        this.body = review.getBody();
        this.isCompleted = review.getIsCompleted();
        this.note = new NoteResponseDTO(review.getNote());
        this.tag = new TagResponseDTO.SingleTag(review.getTag());
    }
}
