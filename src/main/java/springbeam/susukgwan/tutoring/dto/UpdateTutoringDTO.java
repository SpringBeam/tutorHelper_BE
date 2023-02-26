package springbeam.susukgwan.tutoring.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTutoringDTO {
    private Long tutoringId;
    private String subject;
    private String startDate;
}
