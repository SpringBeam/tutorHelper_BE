package springbeam.susukgwan.assignment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignment_submit")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Submit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment; // 제출 대상 숙제

    @CreatedDate
    private LocalDateTime dateTime; // 제출시간

    @Column(nullable = false)
    private Long rate; // 평가점수

    @Column(nullable = false, length = 1000)
    @Convert(converter = StringListConverter.class)
    private List<String> imageUrl; // 이미지 url 들
}
