package springbeam.susukgwan.schedule;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.tutoring.Tutoring;

import java.time.LocalDateTime;

// 조회(select)가 많이 일어나고 insert는 적고 delete는 거의 없고 update 또한 적으므로 인덱싱 하면 좋음.
// but, JPA로 자동 테이블 생성 시 기본키, 참조키는 자동 인덱싱되므로 불필요한 코드일 수 있음. 참고용 코드임.
@Entity
@Table(name = "regular_record", indexes = @Index(name = "idx__tutoring_id", columnList = "tutoring_id"))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class RegularRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutoring_id")
    private Tutoring tutoring;

    private LocalDateTime appliedUntil;

    private String dayTimeString;

}
