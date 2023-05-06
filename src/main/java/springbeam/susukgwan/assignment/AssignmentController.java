package springbeam.susukgwan.assignment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.assignment.dto.AssignmentRequestDTO;

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
}
