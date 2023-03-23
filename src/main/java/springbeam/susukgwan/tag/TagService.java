package springbeam.susukgwan.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.review.dto.ReviewDTO;
import springbeam.susukgwan.subject.Subject;
import springbeam.susukgwan.subject.SubjectRepository;
import springbeam.susukgwan.tag.dto.TagDTO;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final SubjectRepository subjectRepository;
    private final TutoringRepository tutoringRepository;

    /* 태그 추가 */
    public String createTag(TagDTO.Create createTag){
        Optional<Subject> subject = subjectRepository.findById(createTag.getSubjectId());
        if (subject.isPresent() && subject.get().getTagList().size() < 10) { // 기존 등록태그가 10개 미만일때만 추가 가능
            Tag tag = Tag.builder()
                        .name(createTag.getTagName())
                        .subject(subject.get())
                        .build();
            tagRepository.save(tag);
            return "SUCCESS";
        }
        return "FAIL";
    }

    public TagDTO.ResponseTagList tagList(Long tutoringId) {
        Optional<Tutoring> tutoring = tutoringRepository.findById(tutoringId);
        TagDTO.ResponseTagList tagList = new TagDTO.ResponseTagList();
        if (tutoring.isPresent()) {
            System.out.println("======================================");
            Subject subject = tutoring.get().getSubject();
            tagList.setCount(subject.getTagList().size());
//            tagList.setTagList(subject.getTagList());
            //List<Response> responseList = reviewList.stream().map(o->new Response(o)).collect(Collectors.toList());
            List<ReviewDTO.Response.ReviewTagDTO> tagDTOList = subject.getTagList().stream().map(o->new ReviewDTO.Response.ReviewTagDTO(o)).collect(Collectors.toList());
            tagList.setTagList(tagDTOList);
        }
        return tagList;
    }
}
