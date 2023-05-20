package springbeam.susukgwan.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbeam.susukgwan.tutoring.Tutoring;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CancellationRepository extends JpaRepository<Cancellation, Long> {
    List<Cancellation> findAllByCancelledDateTime(LocalDateTime dateTime);
    Optional<Cancellation> findByTutorIdAndCancelledDateTime(Long tutorId, LocalDateTime dateTime);
    List<Cancellation> findAllByTutoring(Tutoring tutoring);
}
