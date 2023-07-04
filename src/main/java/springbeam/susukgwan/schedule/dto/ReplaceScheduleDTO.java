package springbeam.susukgwan.schedule.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplaceScheduleDTO {
    private Long tutoringId;
    private String date;
    private String startTime;
    private String endTime;
    private String dateWant;
    private String startTimeWant;
    private String endTimeWant;
}
