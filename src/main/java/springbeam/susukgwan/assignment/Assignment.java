package springbeam.susukgwan.assignment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.note.Note;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "assignment")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 숙제 id

    @Column(nullable = false)
    private String body; // 숙제 내용

    @Column(nullable = false)
    private LocalDate startDate; // 시작일시

    @Column(nullable = false)
    private LocalDate endDate; // 마감일시 (데드라인)

    @Column(nullable = false)
    @Convert(converter = LongListConverter.class)
    private List<Long> frequency; // 제출 빈도 (제출하는 요일)

    @Column(nullable = false)
    private String amount; // 매번 제출하는 분량

    @Column(nullable = false)
    private Boolean isCompleted; // 완료여부 (true:완료, false:미완료)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id")
    private Note note; // 수업일지
}
