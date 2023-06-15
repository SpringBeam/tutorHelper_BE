package springbeam.susukgwan.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.assignment.dto.AssignmentRequestDTO;
import springbeam.susukgwan.review.dto.ReviewRequestDTO;

import java.time.LocalDateTime;
import java.util.List;

public class NoteRequestDTO {
    @Getter
    @Setter
    public static class Create {
        @NotNull
        private Long tutoringId;
        @NotNull
        private LocalDateTime tutoringTime; // 수업일지 생성시간 아니고 수업일지 등록한 수업 일시
        @NotBlank
        private String progress;
        private List<ReviewRequestDTO.Create> reviewList; // 없을수도 있음
        private List<AssignmentRequestDTO.Create> assignmentList; // 없을수도 있음
    }
}
