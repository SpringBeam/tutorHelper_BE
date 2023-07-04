package springbeam.susukgwan.tutoring.color;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/color")
public class ColorController {
    @Autowired
    ColorService colorService;

    @PutMapping("")
    public ResponseEntity setColor(@RequestBody ColorSetDTO colorSetDTO) {
        return colorService.setColor(colorSetDTO);
    }
}
