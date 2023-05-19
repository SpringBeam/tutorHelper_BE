package springbeam.susukgwan.tutoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutoringRepository extends JpaRepository<Tutoring, Long> {
    Optional<Tutoring> findByIdAndTutorId(Long id, Long tutorId);
    List<Tutoring> findAllByTutorId(Long tutorId);
    List<Tutoring> findAllByTuteeId(Long tutorId);
    List<Tutoring> findAllByParentId(Long tutorId);
}
