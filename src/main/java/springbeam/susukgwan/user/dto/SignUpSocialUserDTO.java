package springbeam.susukgwan.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignUpSocialUserDTO {
    private String role;
    private String name;
}
