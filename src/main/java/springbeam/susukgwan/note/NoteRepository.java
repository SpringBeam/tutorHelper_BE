package springbeam.susukgwan.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbeam.susukgwan.tutoring.Tutoring;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findFirst1ByTutoringOrderByDateTimeDesc(Tutoring tutoring);
}
