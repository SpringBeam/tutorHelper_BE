package springbeam.susukgwan.note.dto;

import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.note.Note;

@Getter
@Setter
public class NoteResponseDTO { /* Note 응답 DTO => 나중에 필요에 따라 ID만/일부/모든 필드 반환하는거로 나눌것 */
    private Long id;
    public NoteResponseDTO(Note note) {
        this.id = note.getId();
    }
}
