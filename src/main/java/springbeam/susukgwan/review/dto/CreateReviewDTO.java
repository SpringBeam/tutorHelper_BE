package springbeam.susukgwan.review.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewDTO {
    private Long tutoringId;
    private String body;
    private Long tagId;
}
