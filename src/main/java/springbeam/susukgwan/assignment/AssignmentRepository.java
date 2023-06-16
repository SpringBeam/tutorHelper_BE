package springbeam.susukgwan.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springbeam.susukgwan.note.Note;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @Query(
            value = "SELECT t.tutor_id " +
                    "FROM susukgwan.tutoring AS t " +
                    "JOIN susukgwan.note AS n " +
                    "ON t.id = n.tutoring_id " +
                    "JOIN susukgwan.assignment AS a " +
                    "ON n.id = a.note_id " +
                    "WHERE a.id = :assignmentId",
            nativeQuery = true
    )
    Long GetTutorIdOfAssignment(@Param(value = "assignmentId") Long assignmentId);

    @Query(
            value = "SELECT t.tutee_id " +
                    "FROM susukgwan.tutoring AS t " +
                    "JOIN susukgwan.note AS n " +
                    "ON t.id = n.tutoring_id " +
                    "JOIN susukgwan.assignment AS a " +
                    "ON n.id = a.note_id " +
                    "WHERE a.id = :assignmentId",
            nativeQuery = true
    )
    Long GetTuteeIdOfAssignment(@Param(value = "assignmentId") Long assignmentId);

    @Query(
            value = "SELECT t.parent_id " +
                    "FROM susukgwan.tutoring AS t " +
                    "JOIN susukgwan.note AS n " +
                    "ON t.id = n.tutoring_id " +
                    "JOIN susukgwan.assignment AS a " +
                    "ON n.id = a.note_id " +
                    "WHERE a.id = :assignmentId",
            nativeQuery = true
    )
    Long GetParentIdOfAssignment(@Param(value = "assignmentId") Long assignmentId);

    List<Assignment> findByNote(Note note);
}
