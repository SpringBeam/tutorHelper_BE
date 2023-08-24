package springbeam.susukgwan.assignment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springbeam.susukgwan.assignment.dto.AssignmentRequestDTO;
import springbeam.susukgwan.assignment.dto.SubmitRequestDTO;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assignment")
public class AssignmentController {
    private final AssignmentService assignmentService;
    private final SubmitService submitService;

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

    @PostMapping("/{assignmentId}/check")
    public ResponseEntity<?> checkAssignment (@PathVariable("assignmentId") Long assignmentId, @Valid @RequestBody AssignmentRequestDTO.Check checkAssignment) {
        return assignmentService.checkAssignment(assignmentId, checkAssignment);
    }

    @PostMapping("/{assignmentId}/submit")
    public ResponseEntity<?> submitFiles (@PathVariable("assignmentId") Long assignmentId, @RequestParam("images") List<MultipartFile> multipartFileList) throws IOException {
        return submitService.submitFiles(assignmentId, multipartFileList);
    }

    @DeleteMapping("/submit/{submitId}")
    public ResponseEntity<?> deleteSubmit (@PathVariable("submitId") Long submitId) {
        return submitService.deleteSubmit(submitId);
    }

    @PostMapping("/submit/{submitId}/evaluate")
    public ResponseEntity<?> evaluateSubmit (@PathVariable("submitId") Long submitId, @Valid @RequestBody SubmitRequestDTO.Evaluate evaluateSubmit) {
        return submitService.evaluateSubmit(submitId, evaluateSubmit);
    }

    @GetMapping("/{assignmentId}/submit/list")
    public ResponseEntity<?> listSubmit (@PathVariable("assignmentId") Long assignmentId) {
        return assignmentService.submitListOfAssignment(assignmentId);
    }

    @PostMapping("/list")
    public ResponseEntity<?> listAssignment (@Valid @RequestBody AssignmentRequestDTO.ListRequest listAssignment) {
        return assignmentService.listAssignment(listAssignment);
    }

    @PostMapping("/multi-delete")
    public ResponseEntity<?> multiDeleteAssignment(@Valid @RequestBody AssignmentRequestDTO.MultiDelete deleteAssignmentList) {
        return assignmentService.multiDeleteAssignment(deleteAssignmentList);
    }
}
