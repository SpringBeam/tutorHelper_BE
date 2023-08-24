package springbeam.susukgwan.review;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.note.NoteRepository;
import springbeam.susukgwan.review.dto.ReviewRequestDTO;
import springbeam.susukgwan.review.dto.ReviewResponseDTO;
import springbeam.susukgwan.schedule.DummyScheduleService;
import springbeam.susukgwan.tag.Tag;
import springbeam.susukgwan.tag.TagRepository;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final TutoringRepository tutoringRepository;
    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;
    @Autowired
    private DummyScheduleService dummyScheduleService;

    /* 복습항목 추가 */
    public ResponseEntity<?> createReview(ReviewRequestDTO.Create createReview) {

        Optional<Tutoring> tutoring = tutoringRepository.findById(createReview.getTutoringId());
        Optional<Tag> tag = tagRepository.findById(createReview.getTagId());
        Optional<Note> note = noteRepository.findById(createReview.getNoteId());

        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        if (tutoring.isPresent() && tutoring.get().getTutorId() != userId) { // 해당 수업의 선생님만 접근가능
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
        }
        Long tutorIdOfTag = tagRepository.GetTutorIdOfTag(createReview.getTagId());
        if (tutorIdOfTag != null && (userId != tutorIdOfTag)) { // 해당 태그를 만든 선생님만 접근가능
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
        }
        Long tutorIdOfNote = noteRepository.GetTutorIdOfNote(createReview.getNoteId());
        if (tutorIdOfNote != null && (userId != tutorIdOfNote)) { // 해당 수업일지를 만든 선생님만 접근가능
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
        }

        if (tutoring.isPresent() && tag.isPresent()) {
            if (note.isPresent()) { // 존재하는 노트
                if (note.get().getTutoring() != tutoring.get()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                Review review = Review.builder()
                        .body(createReview.getBody())
                        .isCompleted(false)
                        .note(note.get())
                        .tag(tag.get())
                        .build();
                reviewRepository.save(review);
                return ResponseEntity.ok(review.getId());
            } else { // 존재하지 않는 노트
                Integer noteCount = tutoring.get().getNotes().size();
                if (createReview.getNoteId() == 0 && noteCount == 0) { // 수업노트가 하나도 없어서 더미노트(수업 첫날 자정) 생성하는 경우
                    dummyScheduleService.newDummyIrregularSchedule(tutoring.get(), tutoring.get().getStartDate());
                    Note newNote = Note.builder()
                            .dateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                            .tutoringTime(tutoring.get().getStartDate().atTime(0,0))
                            .tutoring(tutoring.get())
                            .progress(".")
                            .build();
                    Review review = Review.builder()
                            .body(createReview.getBody())
                            .isCompleted(false)
                            .note(newNote)
                            .tag(tag.get())
                            .build();
                    noteRepository.save(newNote);
                    reviewRepository.save(review);
                    return ResponseEntity.ok(review.getId());
                } else if (createReview.getNoteId() != 0) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_NOTE.getMsg()));
                } else { // 수업일지가 이미 있는데 더미노트 생성을 위한 0을 보낸 경우 (그냥안됨)
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            }
        } else if (tutoring.isPresent() && tag.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TAG.getMsg()));
        } else if (tutoring.isEmpty() && tag.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TUTORING.getMsg()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TUTORING_AND_TAG.getMsg()));
        }
    }

    /* 복습항목 수정 */
    public ResponseEntity<?> updateReview(Long reviewId, ReviewRequestDTO.Update updateReview) {
        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_REVIEW.getMsg()));
        }

        Review r = review.get();

        if (updateReview.getBody() != null) {
            if (updateReview.getBody().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.BODY_CONSTRAINTS.getMsg()));
            }
            r.setBody(updateReview.getBody());
        }

        if (updateReview.getTagId() != null) {
            Optional<Tag> tag = tagRepository.findById(updateReview.getTagId());
            if (tag.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TAG.getMsg()));
            }

            Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
            Long tutorIdOfTag = tagRepository.GetTutorIdOfTag(updateReview.getTagId());
            if (tutorIdOfTag != null && (userId != tutorIdOfTag)) { // 해당 태그를 만든 선생님만 접근가능
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
            }

            r.setTag(tag.get());
        }

        reviewRepository.save(r);
        return ResponseEntity.ok().build();
    }

    /* 복습항목 삭제 */
    public ResponseEntity<?> deleteReview(Long reviewId){
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isPresent()) {
            reviewRepository.deleteById(reviewId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_REVIEW.getMsg()));
        }
    }

    /* 복습항목 완료여부 체크 */
    public ResponseEntity<?> checkReview(Long reviewId, ReviewRequestDTO.Check checkReview) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isPresent()) {
            Review r = review.get();
            r.setIsCompleted(checkReview.getIsCompleted());
            reviewRepository.save(r);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_REVIEW.getMsg()));
        }
    }

    /* 복습내역 불러오기 */
    public ResponseEntity<?> reviewList(ReviewRequestDTO.ListRequest listReview) {
        Optional<Tutoring> tutoring = tutoringRepository.findById(listReview.getTutoringId());
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

        List<Review> reviewList = reviewRepository.GetReviewListbyTutoringId(listReview.getTutoringId());
        List<ReviewResponseDTO> responseList = reviewList.stream().map(o->new ReviewResponseDTO(o)).collect(Collectors.toList());

        if (responseList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_REVIEW.getMsg()));
        }

        return ResponseEntity.ok().body(responseList); // DTO로 반환 (순환참조 방지)
    }

    /* 전체 복습내역 리스트 반환 for getTutoringDetail() in tutoringService */
    public List<ReviewResponseDTO> reviewListForDetail(Tutoring tutoring) {
        // tutoring 확인 후 호출됨.
        List<Review> reviewList = reviewRepository.GetReviewListbyTutoringId(tutoring.getId());
        List<ReviewResponseDTO> responseList = reviewList.stream().map(o->new ReviewResponseDTO(o)).collect(Collectors.toList());
        return responseList;
    }

    /* 복습항목 여러개 삭제 */
    public ResponseEntity<?> multiDeleteReview(ReviewRequestDTO.MultiDelete deleteReviewList) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        List<Long> deleteReviewIdList = new ArrayList<>();

        // 제대로 된 요청만 걸러내기
        for (Long reviewId : deleteReviewList.getReviewIdList()) {
            Optional<Review> review = reviewRepository.findById(reviewId);
            if (review.isPresent()) { // 존재하는 복습항목만 고려 (존재하지 않는건 무시)
                Long tutorIdOfReview = reviewRepository.GetTutorIdOfReview(reviewId);
                if (userId == tutorIdOfReview) {
                    deleteReviewIdList.add(reviewId);
                } else {
                    // 권한이 없는 복습항목 삭제하려고 하면 에러처리
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
                }
            }
        }

        // 권한 있는 & 존재하는 항목들 한번에 삭제
        for (Long reviewId : deleteReviewIdList) {
            reviewRepository.deleteById(reviewId);
        }
        return ResponseEntity.ok().build();
    }
}
