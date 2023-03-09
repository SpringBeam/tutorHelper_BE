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

        List<Review> reviewList = new ArrayList<>(); // 복습 리스트 선언
        Optional<Tutoring> tutoring = tutoringRepository.findById(tutoringId); // 해당 수업 가져오기

        if (tutoring.isPresent()) { // 존재하는 수업이면
            List<Note> notes = noteRepository.findByTutoring(tutoring.get()); // 그 수업에 딸린 수업일지들 가져옴
            ListIterator<Note> noteIterator = notes.listIterator(); // 수업일지 리스트 => iterator 로 변환
            while(noteIterator.hasNext()) { // 하나씩 돌면서
                List<Review> reviews = reviewRepository.findByNote(noteIterator.next()); // 그 수업일지에 딸린 복습항목들 가져오기
                reviewList.addAll(reviews); // 복습 항목들 다 붙여서 리스트 하나로 만들기
            }
        }

        return reviewList;
    }
}
