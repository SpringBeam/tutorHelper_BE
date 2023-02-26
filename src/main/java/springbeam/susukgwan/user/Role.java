package springbeam.susukgwan.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    TUTOR("TUTOR"),
    TUTEE("TUTEE"),
    PARENT("PARENT");

    private final String role;
}
