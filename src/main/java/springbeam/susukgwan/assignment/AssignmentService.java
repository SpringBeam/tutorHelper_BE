package springbeam.susukgwan.assignment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.S3Service;
import springbeam.susukgwan.assignment.dto.AssignmentRequestDTO;
import springbeam.susukgwan.assignment.dto.SubmitResponseDTO;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.note.NoteRepository;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final TutoringRepository tutoringRepository;
    private final NoteRepository noteRepository;
    private final SubmitRepository submitRepository;
    private final S3Service s3Service;

    /* 숙제 추가 */
    public ResponseEntity<?> createAssignment(AssignmentRequestDTO.Create createAssignment) {

        /* Authorization - 이 수업의 선생님인 유저만 접근 가능 */
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        Optional<Tutoring> tutoring = tutoringRepository.findById(createAssignment.getTutoringId());
        if (tutoring.isPresent()) {
            if (tutoring.get().getTutorId() != userId) { // Tutoring에 등록된 선생님과 현재유저(선생님)가 다르면 불가능
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
            }
        /* End */

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
//                assignmentRepository.save(assignment);
                return ResponseEntity.ok(assignment);
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
        assignmentRepository.deleteById(assignmentId);
        return ResponseEntity.ok().build();
    }

    /* 숙제 완료/미완료 체크 */
    public ResponseEntity<?> checkAssignment (Long assignmentId, AssignmentRequestDTO.Check checkAssignment) {
        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        Assignment a = assignment.get();
        a.setIsCompleted(checkAssignment.getIsCompleted());
        assignmentRepository.save(a);
        return ResponseEntity.ok().build();
    }

    /* 숙제의 모든 인증피드 리스트 */
    public ResponseEntity<?> submitListOfAssignment (Long assignmentId) {
        List<Submit> submitList = submitRepository.GetSubmitListByAssignmentId(assignmentId);
        List<SubmitResponseDTO> responseList = submitList.stream().map(o->new SubmitResponseDTO(o, s3Service)).collect(Collectors.toList());

        if (responseList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_SUBMIT.getMsg()));
        }
        return ResponseEntity.ok().body(responseList);
    }
}
