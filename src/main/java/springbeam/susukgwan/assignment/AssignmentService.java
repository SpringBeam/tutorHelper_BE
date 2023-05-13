package springbeam.susukgwan.assignment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.S3Service;
import springbeam.susukgwan.assignment.dto.AssignmentRequestDTO;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.note.NoteRepository;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final TutoringRepository tutoringRepository;
    private final NoteRepository noteRepository;
    private final S3Service s3Service;
    private final SubmitRepository submitRepository;

    /* 숙제 추가 */
    public ResponseEntity<?> createAssignment(AssignmentRequestDTO.Create createAssignment) {

        Optional<Tutoring> tutoring = tutoringRepository.findById(createAssignment.getTutoringId());
        if (tutoring.isPresent()) {
            Optional<Note> note = noteRepository.findFirst1ByTutoringOrderByDateTimeDesc(tutoring.get());
            if (note.isPresent()) {
                Assignment assignment = Assignment.builder()
                        .body(createAssignment.getBody())
                        .startDate(createAssignment.getStartDate())
                        .endDate(createAssignment.getEndDate())
                        .frequency(createAssignment.getFrequency())
                        .amount(createAssignment.getAmount())
                        .isCompleted(false)
                        .note(note.get())
                        .build();
                assignmentRepository.save(assignment);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_NOTE.getMsg()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TUTORING.getMsg()));
        }
    }

    /* 숙제 수정 */
    public ResponseEntity<?> updateAssignment(Long assignmentId, AssignmentRequestDTO.Update updateAssignment) {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);

        if (assignment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_ASSIGNMENT.getMsg()));
        }

        Assignment a = assignment.get();

        if (updateAssignment.getBody() != null) {
            if (updateAssignment.getBody().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.BODY_CONSTRAINTS.getMsg()));
            }
            a.setBody(updateAssignment.getBody());
        }

        if (updateAssignment.getStartDate() != null) {
            a.setStartDate(updateAssignment.getStartDate());
        }

        if (updateAssignment.getEndDate() != null) {
            a.setEndDate(updateAssignment.getEndDate());
        }

        if (updateAssignment.getFrequency() != null) {
            a.setFrequency(updateAssignment.getFrequency());
        }

        if (updateAssignment.getAmount() != null) {
            a.setAmount(updateAssignment.getAmount());
        }

        assignmentRepository.save(a);
        return ResponseEntity.ok().build();
    }

    /* 숙제 삭제 */
    public ResponseEntity<?> deleteAssignment(Long assignmentId) {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if (assignment.isPresent()) {
            assignmentRepository.deleteById(assignmentId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_ASSIGNMENT.getMsg()));
        }
    }

    /* 숙제 인증피드 등록 */
    public ResponseEntity<?> submitFiles (Long assignmentId, List<MultipartFile> multipartFileList) throws IOException {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if (assignment.isPresent()) {
            List<String> imageUrlList = new ArrayList<>();

            for (MultipartFile multipartFile : multipartFileList) {
                imageUrlList.add(s3Service.upload(multipartFile));
            }

            Submit submit = Submit.builder()
                    .assignment(assignment.get())
                    .dateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                    .rate(0L)
                    .imageUrl(imageUrlList)
                    .build();

            submitRepository.save(submit);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_ASSIGNMENT.getMsg()));
        }
    }

    /* 숙제 인증피드 삭제 */
    public ResponseEntity<?> deleteSubmit (Long submitId) {
        Optional<Submit> submit = submitRepository.findById(submitId);
        if (submit.isPresent()) {
            List<String> S3Urls = submit.get().getImageUrl();
            for (String url : S3Urls) {
                s3Service.delete(url); // S3에 업로드된 이미지들 삭제 먼저
            }
            submitRepository.deleteById(submitId); // 숙제 인증피드 삭제
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_SUBMIT.getMsg()));
        }
    }
}
