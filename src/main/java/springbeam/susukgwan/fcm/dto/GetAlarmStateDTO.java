package springbeam.susukgwan.fcm.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetAlarmStateDTO {
    private boolean isAlarmOn;
}
