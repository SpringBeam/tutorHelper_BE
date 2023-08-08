package springbeam.susukgwan.tutoring.color;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColorSetDTO {
    private Long tutoringId;
    private Long color; // 1 ~ 10
}
