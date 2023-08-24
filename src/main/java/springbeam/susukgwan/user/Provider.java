package springbeam.susukgwan.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {
    KAKAO("KAKAO"),
    GOOGLE("GOOGLE"),
    NONE("NONE");
    private final String provider;
}
