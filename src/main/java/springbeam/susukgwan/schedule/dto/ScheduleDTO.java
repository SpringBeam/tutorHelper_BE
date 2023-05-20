package springbeam.susukgwan.schedule.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDTO {
    private Long tutoringId;
    private String date;
    private String startTime;
    private String endTime;
}
