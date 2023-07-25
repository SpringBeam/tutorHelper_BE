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
    private String tutorImage;
    private String tuteeName;
    private String tuteeImage;
    private String parentName;
    private String startDate;
    private List<DayTimeDTO> dayTimeList;
    private List<ScheduleInfoResponseDTO> scheduleList;
    private List<ReviewResponseDTO> reviewList;
    private List<AssignmentResponseDTO> assignmentList;
    private List<NoteSimpleInfoDTO> noteList;
}
