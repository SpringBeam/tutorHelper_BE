package springbeam.susukgwan.tutoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutoringRepository extends JpaRepository<Tutoring, Long> {

}
