package springbeam.susukgwan.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.note.NoteRepository;
import springbeam.susukgwan.review.dto.CreateReviewDTO;
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

    // 새로운 복습 항목 등록
    public String createReview(CreateReviewDTO createReviewDTO) {

        Optional<Tutoring> tutoring = tutoringRepository.findById(createReviewDTO.getTutoringId());
        Optional<Note> note = noteRepository.findById(1L); // 임시 => tutoring의 최근일지로 찾아오기
        Optional<Tag> tag = tagRepository.findById(createReviewDTO.getTagId());

        if (tutoring.isPresent() && note.isPresent() && tag.isPresent()) {
            // tutoring, note, tag 모두 올바르게 존재하면
            Review review = Review.builder()
                    .body(createReviewDTO.getBody())
                    .isCompleted(false)
                    .note(note.get())
                    .tag(tag.get())
                    .build();
            reviewRepository.save(review);
            return "SUCCESS";
        }

        return "FAIL";
    }

    public List<Review> reviewList(Long tutoringId) {
        List<Review> reviewList = reviewRepository.findAll();
        System.out.println(reviewList);
//        List<Review> reviews = reviewRepository
        return reviewList;
    }

    public void deleteReview(Long reviewId){
        reviewRepository.deleteById(reviewId);
    }

    public void updateReview(Long reviewId, CreateReviewDTO updatedReview) {

        Optional<Review> review = reviewRepository.findById(reviewId);
        Review r = review.get();

        if (updatedReview.getBody() != null) {
            r.setBody(updatedReview.getBody());
        }
        if (updatedReview.getTagId() != null) {
            Optional<Tag> tag = tagRepository.findById(updatedReview.getTagId());
            if (tag.isPresent()) {
                r.setTag(tag.get());
            }
        }

        reviewRepository.save(r);
    }

    public void checkReview(Long reviewId, Boolean isCompleted) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        Review r = review.get();
        r.setIsCompleted(isCompleted);
        reviewRepository.save(r);
    }
}
