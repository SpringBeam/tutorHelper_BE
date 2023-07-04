package springbeam.susukgwan.tutoring.color;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColorSetDTO {
    private Long tutoringId;
    private Long colorId; // 1 ~ 10
}
