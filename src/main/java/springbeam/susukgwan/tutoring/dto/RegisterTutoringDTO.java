package springbeam.susukgwan.tutoring.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterTutoringDTO {
    private String subject;
    private List<DayTimeDTO> dayTimeList;
    private String startDate;
}
