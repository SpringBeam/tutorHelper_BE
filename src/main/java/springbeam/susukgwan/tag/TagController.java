package springbeam.susukgwan.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.ResponseCode;
import springbeam.susukgwan.tag.dto.TagDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tag")
public class TagController {
    private final TagService tagService;

    @PostMapping("")
    public ResponseEntity<ResponseCode> createTag (@RequestBody TagDTO.Create createTag) {
        String code = tagService.createTag(createTag);
        ResponseCode responseCode = ResponseCode.builder().code(code).build();
        if (code.equals("SUCCESS")) {
            return new ResponseEntity<>(responseCode, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(responseCode, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/list")
    public TagDTO.ResponseTagList tagList (@RequestBody TagDTO.RequestList tagList) {
        return tagService.tagList(tagList.getTutoringId());
    }
}
