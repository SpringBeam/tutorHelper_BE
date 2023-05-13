package springbeam.susukgwan.assignment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springbeam.susukgwan.assignment.dto.AssignmentRequestDTO;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assignment")
public class AssignmentController {
    private final AssignmentService assignmentService;

    @PostMapping("")
    public ResponseEntity<?> createAssignment (@Valid @RequestBody AssignmentRequestDTO.Create createAssignment) {
        return assignmentService.createAssignment(createAssignment);
    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<?> updateAssignment (@PathVariable("assignmentId") Long assignmentId, @RequestBody AssignmentRequestDTO.Update updateAssignment) {
        return assignmentService.updateAssignment(assignmentId, updateAssignment);
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<?> deleteAssignment (@PathVariable("assignmentId") Long assignmentId) {
        return assignmentService.deleteAssignment(assignmentId);
    }

    @PostMapping("/{assignmentId}/submit")
    public ResponseEntity<?> submitFiles (@PathVariable("assignmentId") Long assignmentId, @RequestParam("images") List<MultipartFile> multipartFileList) throws IOException {
        return assignmentService.submitFiles(assignmentId, multipartFileList);
    }

    @DeleteMapping("/submit/{submitId}")
    public ResponseEntity<?> deleteSubmit (@PathVariable("submitId") Long submitId) {
        return assignmentService.deleteSubmit(submitId);
    }
}
