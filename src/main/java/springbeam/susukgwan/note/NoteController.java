package springbeam.susukgwan.note;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.note.dto.NoteRequestDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/note")
public class NoteController {
    private final NoteService noteService;

    @PostMapping("")
    public ResponseEntity<?> createNote (@Valid @RequestBody NoteRequestDTO.Create createNote) {
        return noteService.createNote(createNote);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<?> updateNote (@PathVariable("noteId") Long noteId, @Valid @RequestBody NoteRequestDTO.Update updateNote) {
        return noteService.updateNote(noteId, updateNote);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote (@PathVariable("noteId") Long noteId) {
        return noteService.deleteNote(noteId);
    }
}
