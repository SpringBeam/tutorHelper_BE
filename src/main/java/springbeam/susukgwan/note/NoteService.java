package springbeam.susukgwan.note;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.S3Service;
import springbeam.susukgwan.assignment.*;
import springbeam.susukgwan.assignment.dto.AssignmentRequestDTO;
import springbeam.susukgwan.fcm.PushService;
import springbeam.susukgwan.note.dto.NoteRequestDTO;
import springbeam.susukgwan.note.dto.NoteResponseDTO;
import springbeam.susukgwan.review.Review;
import springbeam.susukgwan.review.ReviewRepository;
import springbeam.susukgwan.review.dto.ReviewRequestDTO;
import springbeam.susukgwan.schedule.Cancellation;
import springbeam.susukgwan.schedule.Irregular;
import springbeam.susukgwan.schedule.Time;
import springbeam.susukgwan.tag.Tag;
import springbeam.susukgwan.tag.TagRepository;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final TutoringRepository tutoringRepository;
    private final AssignmentService assignmentService;
    private final ReviewRepository reviewRepository;
    private final TagRepository tagRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmitRepository submitRepository;
    private final S3Service s3Service;
    private final PushService pushService;

    /* 수업일지 추가 */
    public ResponseEntity<?> createNote (NoteRequestDTO.Create createNote) {
        // tutoring 존재여부 확인
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Optional<Tutoring> tutoringOptional = tutoringRepository.findById(createNote.getTutoringId());
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TUTORING.getMsg()));
        } else { // tutoring 존재는 하는데
            if (tutoringOptional.get().getTutorId() != userId) { // 내 수업이 아닐때
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
            }
        }

        Tutoring tutoring = tutoringOptional.get();

        // 수업일지 등록할 수 있는 상황인지 확인 (취소된 수업인지, 이미 일지가 등록된 수업인지 등)
        LocalDateTime tutoringTime = createNote.getTutoringTime(); // 등록할 수업일시 (LocalDateTime(날짜+시간))

        List<Note> noteList = tutoring.getNotes(); // 기존 수업일지들 (LocalDateTime(날짜+시간))
        List<Time> timeList = tutoring.getTimes(); // 정규일정들 (dayOfWeek(요일), LocalTime(시간))
        List<Irregular> irregularList = tutoring.getIrregulars(); // 비정규일정들 (LocalDate(날짜), LocalTime(시간))
        List<Cancellation> cancellationList = tutoring.getCancellations(); // 취소일정들 (LocalDateTime(날짜+시간))

        /*
         1. 기존 수업일지에 동일한 tutoringTime을 가진 수업일지가 존재하는지
         => 있다면 FAIL : "이미 등록된 수업일지가 있음"
         => 없다면
            2. tutoringTime이 등록된 정규일정에 부합하는지
            => 맞다면
               3-1. 취소된 일정인지
               => 취소됐다면 FAIL : "이미 취소된 수업임"
               => 아니라면 SUCCESS
            => 아니라면
               3-2. 비정규일정인지
               => 비정규일정이라면 SUCCESS
               => 아니라면 FAIL : "수업일지를 등록할 수 있는 수업일시가 아님"
         */

        // 해당 시간에 이미 수업일지가 만들어져 있는지 확인
        for (Note note : noteList) {
            if (tutoringTime.isEqual(note.getTutoringTime())) { // 이미 등록된 수업일지가 있음
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.EXIST_NOTE.getMsg()));
            }
        }

        List<String> DayOfWeekAndStartTimeList = timeList.stream().map(o->o.getDay() + " " + o.getStartTime()).collect(Collectors.toList());
        String tutoringTimeStr = tutoringTime.getDayOfWeek() + " " + tutoringTime.toLocalTime();

        Note note = new Note();
        Boolean isIrregular = false;

        if (DayOfWeekAndStartTimeList.contains(tutoringTimeStr)) { // 등록된 정규일정과 일치
            // 취소여부 확인
            for (Cancellation cancellation : cancellationList) {
                if (cancellation.getCancelledDateTime().isEqual(tutoringTime)) { // 취소일정과 일치
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.ALREADY_CANCELLED.getMsg()));
                }
            }
            // 정규일정 맞고 취소일정 아님 => 수업일지 생성
            note = Note.builder()
                    .dateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul"))) // 수업일지 생성시간
                    .tutoringTime(createNote.getTutoringTime()) // 수업일시
                    .progress(createNote.getProgress()) // 진도보고
                    .tutoring(tutoring) // 수업
                    .build();
//            noteRepository.save(note);
        } else { // 정규일정이 아님
            // 비정규일정인지 확인
            for (Irregular irregular : irregularList) {
                LocalDateTime irregularTime = LocalDateTime.of(irregular.getDate(), irregular.getStartTime());
                if (irregularTime.isEqual(tutoringTime)) { // 비정규일정과 일치
                    // 비정규일정임 => 수업일지 생성
                    note = Note.builder()
                            .dateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul"))) // 수업일지 생성시간
                            .tutoringTime(createNote.getTutoringTime()) // 수업일시
                            .progress(createNote.getProgress()) // 진도보고
                            .tutoring(tutoring) // 수업
                            .build();
//                    noteRepository.save(note);
                    isIrregular = true;
                }
            }
            if (!isIrregular) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(ResponseMsgList.IMPOSSIBLE_TIME.getMsg()));
            }
        }

        // 복습 생성
        List<Review> createReviewList = new ArrayList<>();
        if (createNote.getReviewList() != null) {
            for (ReviewRequestDTO.Create r : createNote.getReviewList()) {
                // validation 검사
                Map<String, Object> validationTest = new HashMap<>();
                if (r.getBody() == null || r.getBody().isBlank()) {
                    validationTest.put("reviewList - body", "body 은(는) 필수 입력 항목이며 공백을 제외한 문자를 하나 이상 포함해야 합니다.");
                }
                if (r.getTagId() == null) {
                    validationTest.put("reviewList - tagId", "tagId 은(는) 필수 입력 항목입니다.");
                }
                if (!validationTest.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationTest);
                }
                // validation 검사 끝

                // tag 검사
                Optional<Tag> tag = tagRepository.findById(r.getTagId());
                Long tutorIdOfTag = tagRepository.GetTutorIdOfTag(r.getTagId());
                if (tag.isEmpty()) { // 존재하지 않는 태그
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TAG.getMsg()));
                }
                if (tutorIdOfTag != null && (userId != tutorIdOfTag)) { // 해당 태그를 만든 선생님만 접근가능
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
                }
                // tag 검사 끝

                // review 생성
                Review review = Review.builder()
                        .body(r.getBody())
                        .isCompleted(false)
                        .note(note)
                        .tag(tag.get())
                        .build();
                createReviewList.add(review);
                // review 생성 끝
            }
        }

        // 숙제 생성
        List<Assignment> createAssignmentList = new ArrayList<>();
        if (createNote.getAssignmentList() != null) {
            for (AssignmentRequestDTO.Create a : createNote.getAssignmentList()) {
                // validation 검사
                Map<String, Object> validationTest = new HashMap<>();
                if (a.getBody() == null || a.getBody().isBlank()) {
                    validationTest.put("assignmentList - body", "body 은(는) 필수 입력 항목이며 공백을 제외한 문자를 하나 이상 포함해야 합니다.");
                }
                if (a.getStartDate() == null) {
                    validationTest.put("assignmentList - startDate", "startDate 은(는) 필수 입력 항목입니다.");
                }
                if (a.getEndDate() == null) {
                    validationTest.put("assignmentList - endDate", "endDate 은(는) 필수 입력 항목입니다.");
                }
                if (a.getFrequency() == null) {
                    validationTest.put("assignmentList - frequency", "frequency 은(는) 필수 입력 항목입니다.");
                }
                if (a.getAmount() == null) {
                    validationTest.put("assignmentList - amount", "amount 은(는) 필수 입력 항목입니다.");
                }
                if (!validationTest.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationTest);
                }
                // validation 검사 끝

                // assignment 생성
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

                Assignment assignment = Assignment.builder()
                        .body(a.getBody())
                        .startDate(startDate)
                        .endDate(endDate)
                        .frequency(a.getFrequency())
                        .amount(a.getAmount())
                        .isCompleted(false)
                        .note(note)
                        .count(0L)
                        .goalCount(goalCount)
                        .build();
                createAssignmentList.add(assignment);
                // assignment 생성 끝
            }
        }

        // 여기까지 오면 수업일지, 복습, 숙제 모두 생성 가능한 상태

        // DB에 수업일지 저장
        noteRepository.save(note);

        // DB에 복습 저장
        if (!createReviewList.isEmpty()) { // 생성할 review가 있음
            reviewRepository.saveAllAndFlush(createReviewList); // 한번에 생성
        }

        // DB에 숙제 저장
        if (!createAssignmentList.isEmpty()) { // 생성할 assignment가 있음
            assignmentRepository.saveAllAndFlush(createAssignmentList); // 한번에 생성
        }

        pushService.noteCreateNotification(note);
        return ResponseEntity.ok(note.getId());
    }

    /* 수업일지 보기 */
    public ResponseEntity<?> getNote (Long noteId) {
        Optional<Note> note = noteRepository.findById(noteId);
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        if (note.isPresent()) {
            List<Long> users = new ArrayList<>();
            users.add(note.get().getTutoring().getTutorId());
            users.add(note.get().getTutoring().getTuteeId());
            users.add(note.get().getTutoring().getParentId());
            if (!users.contains(userId)) { // 해당 수업의 선생님, 학생, 학부모만 접근 가능
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_NOTE.getMsg()));
        }

        NoteResponseDTO noteResponseDTO = new NoteResponseDTO(note.get());
        return ResponseEntity.ok().body(noteResponseDTO);
    }

    /* 수업일지 수정 (진도보고만) */
    public ResponseEntity<?> updateNote (Long noteId, NoteRequestDTO.Update updateNote) {
        Optional<Note> note = noteRepository.findById(noteId);
        if (note.isPresent()) {
            Note n = note.get();
            n.setProgress(updateNote.getProgress());
            noteRepository.save(n);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_NOTE.getMsg()));
        }
    }

    /* 수업일지 삭제 (복습, 숙제, 숙제 인증피드 모두) */
    public ResponseEntity<?> deleteNote (Long noteId) {
        Optional<Note> note = noteRepository.findById(noteId);
        if (note.isPresent()) {
            List<Review> reviewList = reviewRepository.findByNote(note.get());
            List<Assignment> assignmentList = assignmentRepository.findByNote(note.get());

            if (!reviewList.isEmpty()) {
                reviewRepository.deleteAll(reviewList); // 복습 삭제
            }

            if (!assignmentList.isEmpty()) {
                for (Assignment a : assignmentList) {
                    List<Submit> submitList = submitRepository.findByAssignment(a);
                    for (Submit s : submitList) {
                        List<String> S3Urls = s.getImageUrl();
                        for (String url : S3Urls) {
                            s3Service.delete(url);
                        }
                    }
                    submitRepository.deleteAll(submitList); // 제출파일 삭제
                }
                assignmentRepository.deleteAll(assignmentList); // 숙제 삭제
            }

            noteRepository.deleteById(noteId); // 수업일지 삭제
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_NOTE.getMsg()));
        }
    }
    /* 전체 일지내역 리스트 반환 for getTutoringDetail() in tutoringService */
    public List<Note> noteListForDetail(Tutoring tutoring, int year, int month) {
        LocalDate targetDate = LocalDate.of(year, month, 1);
        List<Note> noteList = noteRepository.findAllByTutoring(tutoring);
        noteList = noteList.stream().filter(n ->
                (n.getTutoringTime().getYear() == targetDate.getYear() && n.getTutoringTime().getMonth() == targetDate.getMonth())
        ).toList();
        return noteList;
    }
    public Optional<Note> noteByTutoringAndTutoringTime(Tutoring tutoring, LocalDateTime tutoringTime) {
        return noteRepository.findByTutoringAndTutoringTime(tutoring, tutoringTime);
    }
}
