package springbeam.susukgwan.tutoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TutoringRepository extends JpaRepository<Tutoring, Long> {
    Optional<Tutoring> findByIdAndTutorId(Long id, Long tutorId);
}
