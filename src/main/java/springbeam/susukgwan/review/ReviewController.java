package springbeam.susukgwan.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.note.NoteRepository;
import springbeam.susukgwan.review.dto.ReviewRequestDTO;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final NoteRepository noteRepository;

    @PostMapping("")
    public ResponseEntity<?> createReview (@Valid @RequestBody ReviewRequestDTO.Create createReview){
        ResponseEntity result = reviewService.createReview(createReview);
        if (result.getStatusCode() == HttpStatus.OK) {
            Review review = (Review) result.getBody();
            reviewRepository.save(review); // DB에 저장
            return ResponseEntity.ok(review.getId()); // 성공일때는 body 빼고 ID만 넣고 다시 반환
        } else if (result.getStatusCode() == HttpStatus.CREATED) { // 수업일지도 같이 생성
            Review review = (Review)(((HashMap<String, Object>) result.getBody()).get("review"));
            Note note = (Note)(((HashMap<String, Object>) result.getBody()).get("note"));
            noteRepository.save(note);
            reviewRepository.save(review);
            return ResponseEntity.ok(review.getId());
        }
        return result; // 오류코드일때는 그대로 반환
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview (@PathVariable("reviewId") Long reviewId, @RequestBody ReviewRequestDTO.Update updateReview) {
        return reviewService.updateReview(reviewId, updateReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview (@PathVariable("reviewId") Long reviewId) {
        return reviewService.deleteReview(reviewId);
    }

    @PostMapping("/{reviewId}/check")
    public ResponseEntity<?> checkReview (@PathVariable("reviewId") Long reviewId, @Valid @RequestBody ReviewRequestDTO.Check checkReview) {
        return reviewService.checkReview(reviewId, checkReview);
    }

    @PostMapping("/list")
    public ResponseEntity<?> listReview (@Valid @RequestBody ReviewRequestDTO.ListRequest listReview){
        return reviewService.reviewList(listReview);
    }

    @PostMapping("/multi-delete")
    public ResponseEntity<?> multiDeleteReview(@Valid @RequestBody ReviewRequestDTO.MultiDelete deleteReviewList) {
        return reviewService.multiDeleteReview(deleteReviewList);
    }
}
