package springbeam.susukgwan.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springbeam.susukgwan.subject.Subject;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Boolean existsByNameAndSubject(String name, Subject subject);

    @Query(
            value = "SELECT s.tutor_id " +
                    "FROM susukgwan.subject AS s " +
                    "JOIN susukgwan.tag AS t " +
                    "ON s.id = t.subject_id " +
                    "WHERE t.id = :tagId",
            nativeQuery = true
    )
    Long GetTutorIdOfTag(@Param(value = "tagId") Long tagId);
}
