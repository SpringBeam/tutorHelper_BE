package springbeam.susukgwan.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.subject.Subject;
import springbeam.susukgwan.tag.dto.TagRequestDTO;
import springbeam.susukgwan.tag.dto.TagResponseDTO;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final TutoringRepository tutoringRepository;

    /* 태그 추가 */
    public ResponseEntity<?> createTag(TagRequestDTO.Create createTag){
        Optional<Tutoring> tutoring = tutoringRepository.findById(createTag.getTutoringId());
        ResponseMsg message = new ResponseMsg("");

        if (tutoring.isPresent()) {
            Subject subject = tutoring.get().getSubject();
            Boolean existTagFlag = tagRepository.existsByNameAndSubject(createTag.getTagName(), subject); // 중복불가
            if (subject.getTagList().size() < 10 && !existTagFlag) { // 기존 등록태그가 10개 미만일때만 추가 가능
                Tag tag = Tag.builder()
                        .name(createTag.getTagName())
                        .subject(subject)
                        .build();
                tagRepository.save(tag);
                return ResponseEntity.ok().build();
            } else {
                message.setMsg("태그가 이미 10개 존재하거나 이미 존재하는 이름의 태그입니다.");
            }
        } else {
            message.setMsg("존재하지 않는 수업입니다.");
        }
        return ResponseEntity.badRequest().body(message);
    }

    /* 해당 수업에 달려있는 모든 태그 리스트 */
    public TagResponseDTO.CountAndTagList tagList(TagRequestDTO.ListRequest listTag) {
        Optional<Tutoring> tutoring = tutoringRepository.findById(listTag.getTutoringId());
        TagResponseDTO.CountAndTagList tagList = new TagResponseDTO.CountAndTagList();
        if (tutoring.isPresent()) {
            Subject subject = tutoring.get().getSubject();
            tagList.setCount(subject.getTagList().size());
            List<TagResponseDTO.SingleTag> tagDTOList = subject.getTagList().stream().map(o->new TagResponseDTO.SingleTag(o)).collect(Collectors.toList());
            tagList.setTagList(tagDTOList);
        }
        return tagList;
    }

    /* 태그 수정 */
    public ResponseEntity<?> updateTag(Long tagId, TagRequestDTO.Update updateTag) {
        Optional<Tag> tag = tagRepository.findById(tagId);
        ResponseMsg message = new ResponseMsg("");
        if (tag.isPresent()) {
            Tag t = tag.get();
            t.setName(updateTag.getTagName());
            tagRepository.save(t);
            return ResponseEntity.ok().build();
        } else {
            message.setMsg("존재하지 않는 태그입니다.");
        }
        return ResponseEntity.badRequest().body(message);
    }

    /* 태그 삭제 */
    public ResponseEntity<?> deleteTag(Long tagId) {
        Optional<Tag> tag = tagRepository.findById(tagId);
        ResponseMsg message = new ResponseMsg("");
        if (tag.isPresent()) {
            tagRepository.delete(tag.get());
            return ResponseEntity.ok().build();
        } else {
            message.setMsg("존재하지 않는 태그입니다.");
        }
        return ResponseEntity.badRequest().body(message);
    }
}
