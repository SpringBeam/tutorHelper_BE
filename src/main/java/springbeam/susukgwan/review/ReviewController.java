package springbeam.susukgwan.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.review.dto.CreateReviewDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("")
    public ResponseEntity createReview (@RequestBody CreateReviewDTO createReviewDTO){
        String code = reviewService.createReview(createReviewDTO);
        if (code == "SUCCESS") {
            return new ResponseEntity("Create Review SUCCESS", HttpStatus.CREATED);
        } else {
            return new ResponseEntity("[ERROR] Create Review FAIL", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{tutoringId}")
    public List<Review> getReview (@PathVariable("tutoringId") Long tutoringId){
        System.out.println(tutoringId);
        return reviewService.reviewList(tutoringId);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity deleteReview (@PathVariable("reviewId") Long reviewId) {
        reviewService.deleteReview(reviewId);
        return new ResponseEntity("Delete Review SUCCESS", HttpStatus.OK);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity updateReview (@PathVariable("reviewId") Long reviewId, @RequestBody CreateReviewDTO updatedReview) {
        reviewService.updateReview(reviewId, updatedReview);
        return new ResponseEntity("Update Review SUCCESS", HttpStatus.OK);
    }

    @PostMapping("/{reviewId}/check")
    public ResponseEntity checkReview (@PathVariable("reviewId") Long reviewId, @RequestBody Review review) {
        reviewService.checkReview(reviewId, review.getIsCompleted());
        return new ResponseEntity("Check Review SUCCESS", HttpStatus.OK);
    }
}
