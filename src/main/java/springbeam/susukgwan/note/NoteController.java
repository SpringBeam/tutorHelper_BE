package springbeam.susukgwan.note;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
