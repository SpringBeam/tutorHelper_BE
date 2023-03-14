package springbeam.susukgwan.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum RoleType {
    USER("USER", "일반 사용자"),
    ADMIN("ADMIN", "관리자");


    private final String code;
    private final String description;

    public static RoleType of(String code) {
        return Arrays.stream(RoleType.values())
                .filter(r -> r.getCode().equals(code))
                .findAny()
                .orElse(null);
    }
}
