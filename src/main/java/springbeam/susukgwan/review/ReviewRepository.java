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

    @Query(
            value = "SELECT t.tutor_id " +
                    "FROM susukgwan.tutoring AS t " +
                    "JOIN susukgwan.note AS n " +
                    "ON t.id = n.tutoring_id " +
                    "JOIN susukgwan.review AS r " +
                    "on n.id = r.note_id " +
                    "WHERE r.id = :reviewId",
            nativeQuery = true
    )
    Long GetTutorIdOfReview(@Param(value = "reviewId") Long reviewId);
}