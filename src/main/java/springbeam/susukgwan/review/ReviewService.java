package springbeam.susukgwan.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
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

        ResponseMsg message = new ResponseMsg("");

        if (tutoring.isPresent() && tag.isPresent()) {
            List<Note> notes = noteRepository.findByTutoringOrderByDateTimeDesc(tutoring.get());
            if (!notes.isEmpty()){
                Note note = notes.get(0); // 최근일지
                Review review = Review.builder()
                        .body(createReview.getBody())
                        .isCompleted(false)
                        .note(note)
                        .tag(tag.get())
                        .build();
                reviewRepository.save(review);
                return ResponseEntity.ok().build();
            } else {
                message.setMsg("수업일지가 존재하지 않는 수업입니다.");
            }
        } else {
            message.setMsg("존재하지 않는 수업이거나 태그입니다.");
        }
        return ResponseEntity.badRequest().body(message);
    }

    /* 복습항목 수정 */
    public ResponseEntity<?> updateReview(Long reviewId, ReviewRequestDTO.Update updateReview) {
        Optional<Review> review = reviewRepository.findById(reviewId);

        ResponseMsg message = new ResponseMsg("");

        if (review.isPresent() && !(updateReview.getBody() == null && updateReview.getTagId() == null)) {
            Review r = review.get();

            if (updateReview.getBody() != null && !updateReview.getBody().isBlank()) {
                r.setBody(updateReview.getBody());
                message.setMsg("[복습내용]");
            }
            if (updateReview.getTagId() != null) {
                Optional<Tag> tag = tagRepository.findById(updateReview.getTagId());
                tag.ifPresent(t -> {
                    r.setTag(t);
                    message.setMsg(message.getMsg() + "[태그]");
                });
            }

            reviewRepository.save(r);
            message.setMsg(message.getMsg() + "이(가) 수정되었습니다.");
            return ResponseEntity.ok().body(message);
        } else {
            message.setMsg("존재하지 않는 복습항목이거나 수정할 내용이 없습니다.");
        }
        return ResponseEntity.badRequest().body(message);
    }

    /* 복습항목 삭제 */
    public ResponseEntity<?> deleteReview(Long reviewId){
        Optional<Review> review = reviewRepository.findById(reviewId);
        ResponseMsg message = new ResponseMsg("");
        if (review.isPresent()) {
            reviewRepository.deleteById(reviewId);
            return ResponseEntity.ok().build();
        } else {
            message.setMsg("존재하지 않는 복습항목입니다.");
        }
        return ResponseEntity.badRequest().body(message);
    }

    /* 복습항목 완료여부 체크 */
    public ResponseEntity<?> checkReview(Long reviewId, ReviewRequestDTO.Check checkReview) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        ResponseMsg message = new ResponseMsg("");
        if (review.isPresent()) {
            Review r = review.get();
            r.setIsCompleted(checkReview.getIsCompleted());
            reviewRepository.save(r);
            return ResponseEntity.ok().build();
        } else {
            message.setMsg("존재하지 않는 복습항목입니다.");
        }
        return ResponseEntity.badRequest().body(message);
    }

    /* 복습내역 불러오기 */
    public List<ReviewResponseDTO> reviewList(ReviewRequestDTO.ListRequest listReview) {
        List<Review> reviewList = reviewRepository.GetReviewListbyTutoringId(listReview.getTutoringId());
        List<ReviewResponseDTO> responseList = reviewList.stream().map(o->new ReviewResponseDTO(o)).collect(Collectors.toList());
        return responseList; // DTO로 반환 (순환참조 방지)
    }
}
