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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final TutoringRepository tutoringRepository;
    private final NoteRepository noteRepository;
    private final TagRepository tagRepository;

    /* 복습항목 추가 */
    public String createReview(ReviewDTO reviewDTO) {

        Optional<Tutoring> tutoring = tutoringRepository.findById(reviewDTO.getTutoringId());
        Optional<Note> note = noteRepository.findById(1L); // 임시 => tutoring 의 최근일지로 찾아오기
        Optional<Tag> tag = tagRepository.findById(reviewDTO.getTagId());

        if (tutoring.isPresent() && note.isPresent() && tag.isPresent()) {
            Review review = Review.builder()
                    .body(reviewDTO.getBody())
                    .isCompleted(false)
                    .note(note.get())
                    .tag(tag.get())
                    .build();
            reviewRepository.save(review);
            return "SUCCESS";
        }

        return "FAIL";
    }

    /* 복습항목 수정 */
    public String updateReview(Long reviewId, ReviewDTO updatedReview) {

        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isPresent()) {
            Review r = review.get();

            if (updatedReview.getBody() != null) {
                r.setBody(updatedReview.getBody());
            }
            if (updatedReview.getTagId() != null) {
                Optional<Tag> tag = tagRepository.findById(updatedReview.getTagId());
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
    public List<Review> reviewList(Long tutoringId) {
        List<Review> reviewList = reviewRepository.findAll();
        System.out.println(reviewList);
//        List<Review> reviews = reviewRepository
        System.out.println(tutoringId);
        return reviewList;
    }
}
