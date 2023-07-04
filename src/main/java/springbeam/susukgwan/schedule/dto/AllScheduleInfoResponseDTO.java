package springbeam.susukgwan.schedule.dto;

import lombok.*;
import springbeam.susukgwan.tutoring.dto.NoteSimpleInfoDTO;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllScheduleInfoResponseDTO {
    private Long tutoringId;
    private String personName;
    private String subject;
    private List<ScheduleInfoResponseDTO> scheduleList;
    private List<NoteSimpleInfoDTO> noteList;
}
