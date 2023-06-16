package springbeam.susukgwan.assignment.dto;

import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.assignment.Assignment;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AssignmentResponseDTO {
    private Long id;
    private String body;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> frequency;
    private String amount;
    private Boolean isCompleted;
    private Long noteId;

    public AssignmentResponseDTO (Assignment assignment) {
        this.id = assignment.getId();
        this.body = assignment.getBody();
        this.startDate = assignment.getStartDate();
        this.endDate = assignment.getEndDate();
        this.frequency = assignment.getFrequency();
        this.amount = assignment.getAmount();
        this.isCompleted = assignment.getIsCompleted();
        this.noteId = assignment.getNote().getId();
    }
}
