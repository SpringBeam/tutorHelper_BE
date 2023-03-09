package springbeam.susukgwan.note;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.review.Review;
import springbeam.susukgwan.tutoring.Tutoring;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutoring_id")
    private Tutoring tutoring; // 수업

    @JsonIgnore
    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Review> reviews = new ArrayList<>(); // 복습
}
