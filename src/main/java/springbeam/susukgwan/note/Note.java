package springbeam.susukgwan.note;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import springbeam.susukgwan.assignment.Assignment;
import springbeam.susukgwan.review.Review;
import springbeam.susukgwan.tutoring.Tutoring;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "note")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime dateTime; // 수업일지 생성시간

    @Column(nullable = false)
    private LocalDateTime tutoringTime; // 수업일지를 작성한 수업의 일시 (날짜 + 시간)

    @Column(nullable = false)
    private String progress; // 진도보고

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutoring_id")
    private Tutoring tutoring; // 수업

    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Review> reviews = new ArrayList<>(); // 복습

    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Assignment> assignments = new ArrayList<>(); // 숙제
}
