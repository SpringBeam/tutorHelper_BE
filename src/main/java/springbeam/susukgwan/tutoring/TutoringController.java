package springbeam.susukgwan.tutoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springbeam.susukgwan.ResponseCode;
import springbeam.susukgwan.tutoring.dto.DeleteTutoringDTO;
import springbeam.susukgwan.tutoring.dto.RegisterTutoringDTO;
import springbeam.susukgwan.tutoring.dto.UpdateTutoringDTO;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tutoring")
public class TutoringController {
    @Autowired
    private TutoringService tutoringService;

    @PostMapping("/register")
    public ResponseCode registerTutoring (@RequestBody RegisterTutoringDTO registerTutoringDTO) {
        String code = tutoringService.registerTutoring(registerTutoringDTO);
        return ResponseCode.builder().code(code).build();
    }
    @PostMapping("/update")
    public ResponseCode updateTutoring (@RequestBody UpdateTutoringDTO updateTutoringDTO) {
        String code = tutoringService.updateTutoring(updateTutoringDTO);
        return ResponseCode.builder().code(code).build();
    }
    @PostMapping("/delete")
    public ResponseCode deleteTutoring (@RequestBody DeleteTutoringDTO deleteTutoringDTO) {
        String code = tutoringService.deleteTutoring(deleteTutoringDTO);
        return ResponseCode.builder().code(code).build();
    }
}
