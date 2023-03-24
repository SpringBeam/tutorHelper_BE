package springbeam.susukgwan.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query(
            value = "SELECT * FROM susukgwan.review WHERE note_id IN (SELECT id FROM susukgwan.note WHERE tutoring_id = :tutoringId) ORDER BY is_completed, id DESC",
            nativeQuery = true
    )
    List<Review> GetReviewListbyTutoringId(@Param(value="tutoringId") Long tutoringId);
}