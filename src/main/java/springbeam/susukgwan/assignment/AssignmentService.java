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
import springbeam.susukgwan.assignment.dto.AssignmentResponseDTO;
import springbeam.susukgwan.assignment.dto.SubmitResponseDTO;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.note.NoteRepository;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
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
            // 목표 제출횟수 계산
            LocalDate startDate = createAssignment.getStartDate();
            LocalDate endDate = createAssignment.getEndDate();
            List<LocalDate> dateList = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());
            Long goalCount = 0L;

            if (createAssignment.getFrequency().isEmpty()) {
                goalCount = 1L;
            }
            else {
                for (LocalDate d : dateList) {
                    if (createAssignment.getFrequency().contains(Long.valueOf(d.getDayOfWeek().getValue()))) {
                        goalCount += 1;
                    }
                }
            }

            if (note.isPresent()) { // 수업일지 있을 때
                Assignment assignment = Assignment.builder()
                        .body(createAssignment.getBody())
                        .startDate(createAssignment.getStartDate())
                        .endDate(createAssignment.getEndDate())
                        .frequency(createAssignment.getFrequency())
                        .amount(createAssignment.getAmount())
                        .isCompleted(false)
                        .note(note.get())
                        .count(0L)
                        .goalCount(goalCount)
                        .build();
//                assignmentRepository.save(assignment);
                return ResponseEntity.ok(assignment);
            } else { // 없을 때 수업 첫시작날 자정으로 수업일지 자동 생성
                Note newNote = Note.builder()
                        .dateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                        .tutoringTime(tutoring.get().getStartDate().atTime(0,0))
                        .tutoring(tutoring.get())
                        .progress(".")
                        .build();
                Assignment assignment = Assignment.builder()
                        .body(createAssignment.getBody())
                        .startDate(createAssignment.getStartDate())
                        .endDate(createAssignment.getEndDate())
                        .frequency(createAssignment.getFrequency())
                        .amount(createAssignment.getAmount())
                        .isCompleted(false)
                        .note(newNote)
                        .count(0L)
                        .goalCount(goalCount)
                        .build();
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("assignment", assignment);
                map.put("note", newNote);
                return ResponseEntity.status(HttpStatus.CREATED).body(map);
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

        Boolean dateChangeFlag = false;

        if (updateAssignment.getStartDate() != null) {
            a.setStartDate(updateAssignment.getStartDate());
            dateChangeFlag = true;
        }

        if (updateAssignment.getEndDate() != null) {
            a.setEndDate(updateAssignment.getEndDate());
            dateChangeFlag = true;
        }

        if (updateAssignment.getFrequency() != null) {
            a.setFrequency(updateAssignment.getFrequency());
            dateChangeFlag = true;
        }

        if (updateAssignment.getAmount() != null) {
            a.setAmount(updateAssignment.getAmount());
        }

        if (dateChangeFlag == true) {
            // 시작날짜, 마감날짜, 제출빈도 중에 하나라도 변하면 목표 제출횟수 다시 계산
            LocalDate startDate = a.getStartDate();
            LocalDate endDate = a.getEndDate();
            List<LocalDate> dateList = startDate.datesUntil(endDate.plusDays(1)).collect(Collectors.toList());
            Long goalCount = 0L;

            if (a.getFrequency().isEmpty()) {
                goalCount = 1L;
            }
            else {
                for (LocalDate d : dateList) {
                    if (a.getFrequency().contains(Long.valueOf(d.getDayOfWeek().getValue()))) {
                        goalCount += 1;
                    }
                }
            }

            a.setGoalCount(goalCount);

            if (a.getCount() >= a.getGoalCount()) {
                a.setIsCompleted(true);
            } else {
                a.setIsCompleted(false);
            }
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

    /* 전체 숙제 내역 */
    public ResponseEntity<?> listAssignment (AssignmentRequestDTO.ListRequest listAssignment) {
        Optional<Tutoring> tutoring = tutoringRepository.findById(listAssignment.getTutoringId());
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        if (tutoring.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TUTORING.getMsg()));
        } else {
            List<Long> users = new ArrayList<>();
            users.add(tutoring.get().getTutorId());
            users.add(tutoring.get().getTuteeId());
            users.add(tutoring.get().getParentId());
            if (!users.contains(userId)) { // 해당 수업의 선생님, 학생, 학부모만 접근 가능
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
            }
        }

        List<Assignment> assignmentList = assignmentRepository.GetAssignmentListByTutoringId(listAssignment.getTutoringId());
        List<AssignmentResponseDTO> responseDTOList = assignmentList.stream().map(o->new AssignmentResponseDTO(o)).collect(Collectors.toList());

        if (responseDTOList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_ASSIGNMENT.getMsg()));
        }

        return ResponseEntity.ok(responseDTOList);
    }
    /* 전체 숙제 내역 반환 for getTutoringDetail() in tutoringService */
    public List<AssignmentResponseDTO> assignmentListForDetail(Tutoring tutoring) {
        List<Assignment> assignmentList = assignmentRepository.GetAssignmentListByTutoringId(tutoring.getId());
        List<AssignmentResponseDTO> responseList = assignmentList.stream().map(o->new AssignmentResponseDTO(o)).collect(Collectors.toList());
        return responseList;
    }

    /* 숙제 여러개 삭제 */
    public ResponseEntity<?> multiDeleteAssignment(AssignmentRequestDTO.MultiDelete deleteAssignmentList) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        List<Long> deleteAssignmentIdList = new ArrayList<>();

        // 제대로 된 요청만 걸러내기
        for (Long assignmentId : deleteAssignmentList.getAssignmentIdList()) {
            Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
            if (assignment.isPresent()) { // 존재하는 숙제만 고려 (존재하지 않는 숙제는 무시)
                Long tutorIdOfAssignment = assignmentRepository.GetTutorIdOfAssignment(assignmentId);
                if (userId == tutorIdOfAssignment) {
                    deleteAssignmentIdList.add(assignmentId);
                } else {
                    // 권한이 없는 숙제 삭제하려고 하면 에러처리
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
                }
            }
        }

        // 권한 있는 & 존재하는 항목들 한번에 삭제
        for (Long assignmentId : deleteAssignmentIdList) {
            assignmentRepository.deleteById(assignmentId);
        }
        return ResponseEntity.ok().build();
    }
}
