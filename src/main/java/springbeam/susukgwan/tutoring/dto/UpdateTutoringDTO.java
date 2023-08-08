package springbeam.susukgwan.tutoring.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTutoringDTO {
    private String subject;
    private String startDate;
    private List<DayTimeDTO> dayTimeList;
}
