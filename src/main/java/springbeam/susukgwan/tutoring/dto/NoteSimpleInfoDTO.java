package springbeam.susukgwan.tutoring.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteSimpleInfoDTO {
    private Long noteId;
    private String date;
    private String startTime;
}
