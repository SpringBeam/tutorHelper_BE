package springbeam.susukgwan.tutoring.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterTutoringDTO {
    private String subject;
    private String dayTime;
    private String startDate;
}
