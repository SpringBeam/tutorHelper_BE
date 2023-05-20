package springbeam.susukgwan.schedule.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetScheduleDTO {
    private Long tutoringId;
    private String yearMonth;
}
