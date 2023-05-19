package springbeam.susukgwan.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.note.NoteRepository;
import springbeam.susukgwan.review.dto.ReviewRequestDTO;
import springbeam.susukgwan.review.dto.ReviewResponseDTO;
import springbeam.susukgwan.tag.Tag;
import springbeam.susukgwan.tag.TagRepository;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

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

    /* 복습항목 추가 */
    public ResponseEntity<?> createReview(ReviewRequestDTO.Create createReview) {

        Optional<Tutoring> tutoring = tutoringRepository.findById(createReview.getTutoringId());
        Optional<Tag> tag = tagRepository.findById(createReview.getTagId());

        if (tutoring.isPresent() && tag.isPresent()) {
            Optional<Note> note = noteRepository.findFirst1ByTutoringOrderByDateTimeDesc(tutoring.get());
            if (note.isPresent()) {
                Review review = Review.builder()
                        .body(createReview.getBody())
                        .isCompleted(false)
                        .note(note.get())
                        .tag(tag.get())
                        .build();
                reviewRepository.save(review);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_NOTE.getMsg()));
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

        if (tutoring.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TUTORING.getMsg()));
        }

        List<Review> reviewList = reviewRepository.GetReviewListbyTutoringId(listReview.getTutoringId());
        List<ReviewResponseDTO> responseList = reviewList.stream().map(o->new ReviewResponseDTO(o)).collect(Collectors.toList());

        if (responseList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_REVIEW.getMsg()));
        }

        return ResponseEntity.ok().body(responseList); // DTO로 반환 (순환참조 방지)
    }
}
