package springbeam.susukgwan.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springbeam.susukgwan.tutoring.Tutoring;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findFirst1ByTutoringOrderByDateTimeDesc(Tutoring tutoring);

    @Query(
            value = "SELECT t.tutor_id " +
                    "FROM susukgwan.tutoring AS t " +
                    "JOIN susukgwan.note AS n " +
                    "ON t.id = n.tutoring_id " +
                    "WHERE n.id = :noteId",
            nativeQuery = true
    )
    Long GetTutorIdOfNote(@Param(value = "noteId") Long noteId);
    List<Note> findAllByTutoring(Tutoring tutoring);
}
