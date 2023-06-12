package springbeam.susukgwan.auth.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpDTO {
    private String userId;
    private String password;
    private String name;
    private String role;
}
