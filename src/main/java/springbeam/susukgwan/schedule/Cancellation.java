package springbeam.susukgwan.schedule;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.tutoring.Tutoring;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutoring_id", nullable = false)
    private Tutoring tutoring;

    @Column(nullable = false)
    private Long tutorId;
}
