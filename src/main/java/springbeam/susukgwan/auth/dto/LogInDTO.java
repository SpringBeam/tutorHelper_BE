package springbeam.susukgwan.auth.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogInDTO {
    private String userId;
    private String password;
}
