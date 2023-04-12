package springbeam.susukgwan.review;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.tag.Tag;

@Entity
@Table(name = "review")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 복습 id

    @Column(nullable = false)
    private String body; // 복습내용

    @Column(nullable = false)
    private Boolean isCompleted; // 완료여부 (true:완료, false:미완료)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id")
    private Note note; // 수업일지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag; // 태그
}
