package springbeam.susukgwan.fcm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "fcm_token")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class FCMToken {
    @Id
    private Long userId;

    private String fcmToken;

    boolean isAlarmOn;
}
