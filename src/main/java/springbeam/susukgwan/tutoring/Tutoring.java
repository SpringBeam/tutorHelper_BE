package springbeam.susukgwan.tutoring;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.schedule.Cancellation;
import springbeam.susukgwan.schedule.Irregular;
import springbeam.susukgwan.schedule.Time;
import springbeam.susukgwan.subject.Subject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tutoring")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Tutoring {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject; // 과목

    @Column(nullable = false)
    private LocalDate startDate; // 과외 시작 날짜 (유저 선택 날짜)

    @OneToMany(mappedBy = "tutoring", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Time> times = new ArrayList<>(); // 튜터링 삭제 시 관련 정규시간이 모두 삭제되도록, 연관 없는 정규시간 또한 삭제.

    @OneToMany(mappedBy = "tutoring", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Cancellation> cancellations = new ArrayList<>(); // 튜터링 삭제 시 관련 정규취소가 삭제됨.

    @OneToMany(mappedBy = "tutoring", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Irregular> irregulars = new ArrayList<>(); // 튜터링 삭제 시 관련 비정규일정이 삭제됨.

    @Column(nullable = false)
    private Long tutorId; // 선생 ID
    
    private Long tuteeId; // 학생 ID

    private Long parentId; // 부모 ID

}
