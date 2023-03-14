package springbeam.susukgwan.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.note.NoteRepository;
import springbeam.susukgwan.review.dto.ReviewDTO;
import springbeam.susukgwan.tag.Tag;
import springbeam.susukgwan.tag.TagRepository;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
    public String createReview(ReviewDTO.Create createReview) {

        Optional<Tutoring> tutoring = tutoringRepository.findById(createReview.getTutoringId());
        Optional<Tag> tag = tagRepository.findById(createReview.getTagId());

        if (tutoring.isPresent() && tag.isPresent()) {
            List<Note> notes = noteRepository.findByTutoring(tutoring.get());
            Note note = notes.get(0); // 최근일지
            if (note != null) {
                Review review = Review.builder()
                        .body(createReview.getBody())
                        .isCompleted(false)
                        .note(note)
                        .tag(tag.get())
                        .build();
                reviewRepository.save(review);
                return "SUCCESS";
            }
        }

        return "FAIL";
    }

    /* 복습항목 수정 */
    public String updateReview(Long reviewId, ReviewDTO.Update updateReview) {

        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isPresent()) {
            Review r = review.get();

            if (updateReview.getBody() != null) {
                r.setBody(updateReview.getBody());
            }
            if (updateReview.getTagId() != null) {
                Optional<Tag> tag = tagRepository.findById(updateReview.getTagId());
                tag.ifPresent(r::setTag);
            }

            reviewRepository.save(r);
            return "SUCCESS";
        }

        return "FAIL";
    }

    /* 복습항목 삭제 */
    public String deleteReview(Long reviewId){
        reviewRepository.deleteById(reviewId);
        return "SUCCESS";
    }

    /* 복습항목 완료여부 체크 */
    public String checkReview(Long reviewId, Boolean isCompleted) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isPresent()) {
            Review r = review.get();
            r.setIsCompleted(isCompleted);
            reviewRepository.save(r);
            return "SUCCESS";
        }
        return "FAIL";
    }

    /* 복습내역 불러오기 */
    public List<ReviewDTO.Response> reviewList(Long tutoringId) {
        List<Review> reviewList = reviewRepository.GetReviewListbyTutoringId(tutoringId);
        return ReviewDTO.Response.ResponseList(reviewList); // DTO로 반환 (순환참조 방지)
    }
}
