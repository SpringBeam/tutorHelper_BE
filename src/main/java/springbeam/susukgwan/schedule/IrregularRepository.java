package springbeam.susukgwan.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbeam.susukgwan.tutoring.Tutoring;

import java.util.List;

@Repository
public interface IrregularRepository extends JpaRepository<Irregular, Long> {
    List<Irregular> findAllByTutorId(Long tutorId);
    List<Irregular> findAllByTutoring(Tutoring tutoring);
}
