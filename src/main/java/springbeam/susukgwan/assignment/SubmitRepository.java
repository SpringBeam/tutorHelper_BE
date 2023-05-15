package springbeam.susukgwan.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmitRepository extends JpaRepository<Submit, Long> {
    @Query(
            value = "SELECT * FROM susukgwan.assignment_submit WHERE assignment_id = :assignmentId",
            nativeQuery = true
    )
    List<Submit> GetSubmitListByAssignmentId(@Param(value="assignmentId") Long assignmentId);
}
