package springbeam.susukgwan.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.ResponseCode;
import springbeam.susukgwan.review.dto.ReviewDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping("")
    public ResponseEntity<ResponseCode> createReview (@RequestBody ReviewDTO createReview){
        String code = reviewService.createReview(createReview);
        ResponseCode responseCode = ResponseCode.builder().code(code).build();
        if (code.equals("SUCCESS")) {
            return new ResponseEntity<>(responseCode, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(responseCode, HttpStatus.BAD_REQUEST);
        }
    }


    @PutMapping("/{reviewId}")
    public ResponseEntity<ResponseCode> updateReview (@PathVariable("reviewId") Long reviewId, @RequestBody ReviewDTO updatedReview) {
        String code = reviewService.updateReview(reviewId, updatedReview);
        ResponseCode responseCode = ResponseCode.builder().code(code).build();
        if (code.equals("SUCCESS")) {
            return new ResponseEntity<>(responseCode, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(responseCode, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ResponseCode> deleteReview (@PathVariable("reviewId") Long reviewId) {
        String code = reviewService.deleteReview(reviewId);
        ResponseCode responseCode = ResponseCode.builder().code(code).build();
        if (code.equals("SUCCESS")) {
            return new ResponseEntity<>(responseCode, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(responseCode, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{reviewId}/check")
    public ResponseEntity<ResponseCode> checkReview (@PathVariable("reviewId") Long reviewId, @RequestBody ReviewDTO review) {
        String code = reviewService.checkReview(reviewId, review.getIsCompleted());
        ResponseCode responseCode = ResponseCode.builder().code(code).build();
        if (code.equals("SUCCESS")) {
            return new ResponseEntity<>(responseCode, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(responseCode, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/list/{tutoringId}")
    public List<Review> getReview (@PathVariable("tutoringId") Long tutoringId){
        System.out.println(tutoringId);
        return reviewService.reviewList(tutoringId);
    }
}
