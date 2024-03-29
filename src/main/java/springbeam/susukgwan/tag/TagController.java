package springbeam.susukgwan.tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springbeam.susukgwan.tag.dto.TagRequestDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tag")
public class TagController {
    private final TagService tagService;

    @PostMapping("")
    public ResponseEntity<?> createTag (@Valid @RequestBody TagRequestDTO.Create createTag) {
        return tagService.createTag(createTag);
    }

    @PostMapping("/list")
    public ResponseEntity<?> tagList (@Valid @RequestBody TagRequestDTO.ListRequest listTag) {
        return tagService.tagList(listTag);
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<?> updateTag(@PathVariable("tagId") Long tagId, @Valid @RequestBody TagRequestDTO.Update updateTag) {
        return tagService.updateTag(tagId, updateTag);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<?> deleteTag(@PathVariable("tagId") Long tagId) {
        return tagService.deleteTag(tagId);
    }
}
