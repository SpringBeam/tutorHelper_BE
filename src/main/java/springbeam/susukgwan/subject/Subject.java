package springbeam.susukgwan.subject;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.tag.Tag;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.user.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subject")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 고유번호
    @Column(nullable = false)
    private String name; // 과목명
    @Column(nullable = false)
    private Long tutorId; // 튜터고유번호
//    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
//    private final List<Tutoring> tutoringList = new ArrayList<>(); // 이 과목에 개설된 수업들
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Tag> tagList = new ArrayList<>(); // 이 과목에 달린 태그들
}
