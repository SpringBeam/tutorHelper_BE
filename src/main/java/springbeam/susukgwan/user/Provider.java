package springbeam.susukgwan.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {
    KAKAO("KAKAO"),
    NONE("NONE");
    private final String provider;
}
