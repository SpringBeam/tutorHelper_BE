package springbeam.susukgwan.review.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long tutoringId;
    private String body;
    private Long tagId;
    private Boolean isCompleted;
}
