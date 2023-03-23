package springbeam.susukgwan.review.dto;

import lombok.*;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.review.Review;
import springbeam.susukgwan.tag.dto.TagDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewDTO {

    @Getter
    @Setter
    public static class Create {
        private Long tutoringId;
        private String body;
        private Long tagId;
    }

    @Getter
    @Setter
    public static class Update {
        private String body;
        private Long tagId;
    }

    @Getter
    @Setter
    public static class Check {
        private Boolean isCompleted;
    }

    @Getter
    @Setter
    public static class ListRequest {
        private Long tutoringId;
    }

    @Getter
    @Setter
    public static class Response {
        private Long id;
        private String body;
        private Boolean isCompleted;
        private ReviewNoteDTO note;
        private TagDTO.ResponseTag tag;

        /* Entity -> DTO Response */
        public Response(Review review) {
            this.id = review.getId();
            this.body = review.getBody();
            this.isCompleted = review.getIsCompleted();
            this.note = new ReviewNoteDTO(review.getNote());
            this.tag = new TagDTO.ResponseTag(review.getTag());
        }

        /* Entity List -> DTO Response List */
        public static List<Response> ResponseList (List<Review> reviewList) {
            List<Response> responseList = reviewList.stream().map(o->new Response(o)).collect(Collectors.toList());
            return responseList;
        }

        @Getter
        public static class ReviewNoteDTO {
            private Long id;

            public ReviewNoteDTO(Note note) {
                this.id = note.getId();
            }
        }
    }
}
