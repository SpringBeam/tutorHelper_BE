package springbeam.susukgwan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.util.Optional;


// 개발 후 서버에 올리기 전에는 아래의 SpringBootTest를 주석처리 시켜야 함. test 띄우기가 안 된다는 에러 메시지와 함께 빌드가 막히기 때문.
@SpringBootTest
public class TutoringDeleteTest {
    @Autowired
    private TutoringRepository tutoringRepository;

    @Test
    public void deleteTutoring() {
        Optional<Tutoring> byId = tutoringRepository.findById(1L);
        if (byId.isPresent()) {
            tutoringRepository.delete(byId.get());
        }
        /* test 결과 mysql workbench에서는 foreign key constraints 때문에 삭제가 안 되지만, JPA에서 삭제 시 제대로 tutoring과 time 모두 삭제됨. */
    }
}
