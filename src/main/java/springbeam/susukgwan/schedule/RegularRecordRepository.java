package springbeam.susukgwan.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbeam.susukgwan.tutoring.Tutoring;

import java.util.List;

@Repository
public interface RegularRecordRepository extends JpaRepository<RegularRecord, Long> {
    // List<RegularRecord> findAllByTutoring(Tutoring tutoring);
    List<RegularRecord> findAllByTutoringOrderByAppliedUntilAsc(Tutoring tutoring);
}
