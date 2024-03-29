package springbeam.susukgwan.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
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
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        if (tutoring.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TUTORING.getMsg()));
        } else {
            if (tutoring.get().getTutorId() != userId) { // 해당 수업의 선생님만 접근 가능
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
            }
        }

        Subject subject = tutoring.get().getSubject();
        Boolean existTagFlag = tagRepository.existsByNameAndSubject(createTag.getTagName(), subject); // 중복불가

        if (subject.getTagList().size() < 10 && !existTagFlag) { // 기존 등록태그가 10개 미만일때만 추가 가능
            Tag tag = Tag.builder()
                    .name(createTag.getTagName())
                    .subject(subject)
                    .build();
            tagRepository.save(tag);
            return ResponseEntity.ok(tag.getId());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.TAG_CONSTRAINTS.getMsg()));
        }

    }

    /* 해당 수업에 달려있는 모든 태그 리스트 */
    public ResponseEntity<?> tagList(TagRequestDTO.ListRequest listTag) {
        Optional<Tutoring> tutoring = tutoringRepository.findById(listTag.getTutoringId());
        TagResponseDTO.CountAndTagList tagList = new TagResponseDTO.CountAndTagList();

        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());

        if (tutoring.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TUTORING.getMsg()));
        } else {
            if (tutoring.get().getTutorId() != userId) { // 해당 수업의 선생님만 접근 가능
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
            }
        }

        Subject subject = tutoring.get().getSubject();
        tagList.setCount(subject.getTagList().size());
        List<TagResponseDTO.SingleTag> tagDTOList = subject.getTagList().stream().map(o->new TagResponseDTO.SingleTag(o)).collect(Collectors.toList());
        tagList.setTagList(tagDTOList);

        if (tagList.getTagList().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TAG.getMsg()));
        }

        return ResponseEntity.ok().body(tagList);
    }

    /* 태그 수정 */
    public ResponseEntity<?> updateTag(Long tagId, TagRequestDTO.Update updateTag) {
        Optional<Tag> tag = tagRepository.findById(tagId);
        if (tag.isPresent()) {
            Tag t = tag.get();
            t.setName(updateTag.getTagName());
            tagRepository.save(t);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TAG.getMsg()));
        }
    }

    /* 태그 삭제 */
    public ResponseEntity<?> deleteTag(Long tagId) {
        Optional<Tag> tag = tagRepository.findById(tagId);
        if (tag.isPresent()) {
            tagRepository.delete(tag.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_TAG.getMsg()));
        }
    }
}
