package springbeam.susukgwan.fcm.dto;

import lombok.*;
import springbeam.susukgwan.fcm.Push;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushListDTO {
    private List<Push> pushList;
}
