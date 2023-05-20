package springbeam.susukgwan.schedule.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleInfoResponseDTO {
    private String date;
    private String startTime;
    private String endTime;
}
