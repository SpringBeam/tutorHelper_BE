package springbeam.susukgwan.schedule;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancellation")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
/* To save cancelled 'regular' time schedule */
public class Cancellation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime cancelledDateTime; // 취소된 정규일정의 시작시간

    @Column(nullable = false)
    private Long tutoringId;
}
