package springbeam.susukgwan.review.dto;

import lombok.*;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.review.Review;
import springbeam.susukgwan.tag.Tag;

@Getter
@Setter
public class ReviewDTO {
    private Long id;
    private String body;
    private Boolean isCompleted;
    private NoteDTO note;
    private TagDTO tag;

    public ReviewDTO(Review review) {
        this.id = review.getId();
        this.body = review.getBody();
        this.isCompleted = review.getIsCompleted();
        this.note = new NoteDTO(review.getNote());
        this.tag = new TagDTO(review.getTag());
    }

    @Getter
    public static class NoteDTO {
        private Long id;

        public NoteDTO(Note note){
            this.id = note.getId();
        }
    }

    @Getter
    public static class TagDTO {
        private Long id;
        private String name;
        public TagDTO(Tag tag){
            this.id = tag.getId();
            this.name = tag.getName();
        }
    }
}
