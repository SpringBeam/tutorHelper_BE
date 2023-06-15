package springbeam.susukgwan.auth.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginSuccessResponseDTO {
    private String accessToken;
    private String refreshToken;
    private Long accessExpired;
    private boolean isEnabled;
    private Long userId; // user의 db pk 가입 시, 로그인 시 사용되는 userId와는 다름.
}
