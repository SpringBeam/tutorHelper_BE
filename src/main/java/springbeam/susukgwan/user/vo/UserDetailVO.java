package springbeam.susukgwan.user.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDetailVO {
    private String role;
    private String name;
}
