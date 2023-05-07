package springbeam.susukgwan.tutoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.ResponseCode;
import springbeam.susukgwan.tutoring.dto.*;
import springbeam.susukgwan.user.Role;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tutoring")
public class TutoringController {
    @Autowired
    private TutoringService tutoringService;

    @PostMapping("")
    public ResponseEntity registerTutoring (@RequestBody RegisterTutoringDTO registerTutoringDTO) {
        return tutoringService.registerTutoring(registerTutoringDTO);
    }
    @PutMapping("/{tutoringId}")
    public ResponseEntity updateTutoring (@PathVariable("tutoringId") Long tutoringId, @RequestBody UpdateTutoringDTO updateTutoringDTO) {
        return tutoringService.updateTutoring(tutoringId, updateTutoringDTO);
    }
    @DeleteMapping("/{tutoringId}")
    public ResponseEntity deleteTutoring (@PathVariable("tutoringId") Long tutoringId) {
        return tutoringService.deleteTutoring(tutoringId);
    }
    @PostMapping("/{tutoringId}/invite/tutee")
    public ResponseEntity inviteTutee(@PathVariable("tutoringId") Long tutoringId) {
        return tutoringService.invite(tutoringId, Role.TUTEE);
    }
    @PostMapping("/{tutoringId}/invite/parent")
    public ResponseEntity inviteParent(@PathVariable("tutoringId") Long tutoringId) {
        return tutoringService.invite(tutoringId, Role.PARENT);
    }
    @PostMapping("/invite/approve")
    public ResponseEntity approveInvitation(@RequestBody InvitationCodeDTO invitationCodeDTO) {
        return tutoringService.approveInvitation(invitationCodeDTO);
    }
    @DeleteMapping("/{tutoringId}/withdraw")
    public ResponseEntity withdrawFromTutoring(@PathVariable("tutoringId") Long tutoringId) {
        return tutoringService.withdrawFromTutoring(tutoringId);
    }
    @GetMapping("/list")
    public ResponseEntity getTutoringList() {
        return tutoringService.getTutoringList();
    }
}
