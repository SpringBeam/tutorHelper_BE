package springbeam.susukgwan.schedule.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleListByDayDTO {
    private Long tutoringId;
    private String subject;
    private String personName;
    private String profileImageUrl;
    private int color;
    private String startTime;
    private String endTime;
}
