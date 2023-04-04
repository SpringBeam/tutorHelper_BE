package springbeam.susukgwan.tutoring.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTutoringDTO {
    private String subject;
    private String startDate;
}
