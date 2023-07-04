package springbeam.susukgwan.tutoring.color;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.tutoring.Tutoring;

@Entity
@Table(name = "tutoring")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="tutoring_id")
    private Tutoring tutoring;

    @Column(nullable = false)
    @Builder.Default
    private ColorList tutorColor = ColorList.c0;

    @Column(nullable = false)
    @Builder.Default
    private ColorList tuteeColor = ColorList.c0;
}
