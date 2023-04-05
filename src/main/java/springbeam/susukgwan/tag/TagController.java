package springbeam.susukgwan.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.tag.dto.TagRequestDTO;
import springbeam.susukgwan.tag.dto.TagResponseDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tag")
public class TagController {
    private final TagService tagService;

    @PostMapping("")
    public ResponseEntity<?> createTag (@RequestBody TagRequestDTO.Create createTag) {
        return tagService.createTag(createTag);
    }

    @GetMapping("/list")
    public TagResponseDTO.CountAndTagList tagList (@RequestBody TagRequestDTO.ListRequest listRequest) {
        return tagService.tagList(listRequest.getTutoringId());
    }
}
