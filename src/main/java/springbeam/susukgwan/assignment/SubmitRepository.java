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

    @Query(
            value = "SELECT t.tutee_id " +
                    "FROM susukgwan.tutoring AS t " +
                    "JOIN susukgwan.note AS n " +
                    "ON t.id = n.tutoring_id " +
                    "JOIN susukgwan.assignment AS a " +
                    "ON n.id = a.note_id " +
                    "JOIN susukgwan.assignment_submit AS a_s " +
                    "ON a.id = a_s.assignment_id " +
                    "WHERE a_s.id = :submitId",
            nativeQuery = true
    )
    Long GetTuteeIdOfSubmit(@Param(value = "submitId") Long submitId);

    @Query(
            value = "SELECT t.tutor_id " +
                    "FROM susukgwan.tutoring AS t " +
                    "JOIN susukgwan.note AS n " +
                    "ON t.id = n.tutoring_id " +
                    "JOIN susukgwan.assignment AS a " +
                    "ON n.id = a.note_id " +
                    "JOIN susukgwan.assignment_submit AS a_s " +
                    "ON a.id = a_s.assignment_id " +
                    "WHERE a_s.id = :submitId",
            nativeQuery = true
    )
    Long GetTutorIdOfSubmit(@Param(value = "submitId") Long submitId);
}
