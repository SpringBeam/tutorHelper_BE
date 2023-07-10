package springbeam.susukgwan.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDetailDTO {
    private String role;
    private String name;
    private String userId;
}
