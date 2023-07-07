package springbeam.susukgwan.tutoring.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DayTimeDTO {
    private int day;
    private String startTime;
    private String endTime;
}
