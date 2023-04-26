package springbeam.susukgwan.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.review.dto.ReviewRequestDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("")
    public ResponseEntity<?> createReview (@Valid @RequestBody ReviewRequestDTO.Create createReview){
        return reviewService.createReview(createReview);
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
}
