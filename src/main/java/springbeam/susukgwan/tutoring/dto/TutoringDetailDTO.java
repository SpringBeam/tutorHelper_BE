package springbeam.susukgwan.tutoring.dto;

import lombok.*;
import springbeam.susukgwan.assignment.dto.AssignmentResponseDTO;
import springbeam.susukgwan.review.dto.ReviewResponseDTO;
import springbeam.susukgwan.schedule.dto.ScheduleInfoResponseDTO;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TutoringDetailDTO {
    private Long tutoringId;
    private int color;
    private String subject;
    private String tutorName;
    private String tuteeName;
    private String parentName;
    private String startDate;
    private String dayTime;
    private List<ScheduleInfoResponseDTO> scheduleList;
    private List<ReviewResponseDTO> reviewList;
    private List<AssignmentResponseDTO> assignmentList;
    private List<NoteSimpleInfoDTO> noteList;
}
