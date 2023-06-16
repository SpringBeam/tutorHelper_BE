package springbeam.susukgwan.note.dto;

import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.assignment.Assignment;
import springbeam.susukgwan.assignment.dto.AssignmentResponseDTO;
import springbeam.susukgwan.note.Note;
import springbeam.susukgwan.review.Review;
import springbeam.susukgwan.review.dto.ReviewResponseDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NoteResponseDTO { /* Note 응답 DTO */
    private Long id;
    private LocalDateTime tutoringTime;
    private String progress;
    private List<ReviewResponseDTO> reviewList = new ArrayList<>();
    private List<AssignmentResponseDTO> assignmentList = new ArrayList<>();

    public NoteResponseDTO(Note note) {
        this.id = note.getId();
        this.tutoringTime = note.getTutoringTime();
        this.progress = note.getProgress();
        for (Review r : note.getReviews()) {
            this.reviewList.add(new ReviewResponseDTO(r));
        }
        for (Assignment a : note.getAssignments()) {
            this.assignmentList.add(new AssignmentResponseDTO(a));
        }
    }
}
