package springbeam.susukgwan.schedule;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "cancellation")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Irregular {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date; // 비정규 일정의 날짜

    @Column(nullable = false)
    private LocalTime startDateTime; // 시작 시간

    @Column(nullable = false)
    private LocalTime endDateTime; // 종료 시간

    @Column(nullable = false)
    private Long tutoringId;
}
