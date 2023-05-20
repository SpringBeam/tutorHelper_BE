package springbeam.susukgwan.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TimeRepository extends JpaRepository<Time, Long> {
    @Modifying
    @Transactional
    @Query(
            value= "DELETE FROM susukgwan.time WHERE tutoring_id = :tutoringId",
            nativeQuery = true
    )
    void deleteByTutoringId(@Param(value="tutoringId") Long tutoringId);
}
