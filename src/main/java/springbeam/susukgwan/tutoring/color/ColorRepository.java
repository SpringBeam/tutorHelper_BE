package springbeam.susukgwan.tutoring.color;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbeam.susukgwan.tutoring.Tutoring;

import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    Optional<Color> findByTutoring(Tutoring tutoring);
}
