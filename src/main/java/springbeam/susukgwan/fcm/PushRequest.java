package springbeam.susukgwan.fcm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PushRequest {
    private String title;   // 제목
    private String body; // 내용
    private String topic;   // 주제별 전달에 사용되는 topic 개념
    private String token;
}
