package springbeam.susukgwan.schedule;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.tutoring.Tutoring;

import java.time.DayOfWeek;
import java.time.LocalTime;

/* 정규 일정의 요일, 시간 */
@Entity
@Table(name = "time")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Time {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private DayOfWeek day; // 요일

    private LocalTime startTime; // 시작 시간

    private LocalTime endTime; // 종료 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutoring_id")
    private Tutoring tutoring;
}
