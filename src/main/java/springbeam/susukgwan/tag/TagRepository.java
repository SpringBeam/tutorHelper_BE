package springbeam.susukgwan.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbeam.susukgwan.subject.Subject;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Boolean existsByNameAndSubject(String name, Subject subject);
}
