package springbeam.susukgwan.assignment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.S3Service;
import springbeam.susukgwan.assignment.dto.SubmitRequestDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitService {
    private final SubmitRepository submitRepository;
    private final S3Service s3Service;
    private final AssignmentRepository assignmentRepository;

    /* 숙제 인증피드 등록 */
    public ResponseEntity<?> submitFiles (Long assignmentId, List<MultipartFile> multipartFileList) throws IOException {

        /* Authorization - 이 숙제에 할당되어 있는 학생만 접근 가능 */
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Long tuteeIdOfAssignment = assignmentRepository.GetTuteeIdOfAssignment(assignmentId);
        if ((tuteeIdOfAssignment == null) || (tuteeIdOfAssignment != null && userId != tuteeIdOfAssignment)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
        }
        /* End */

        if (multipartFileList.size() > 3) { // 인증사진은 최대 3개
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.SUBMIT_CONSTRAINTS.getMsg()));
        }

        Optional<Assignment> assignment = assignmentRepository.findById(assignmentId);
        if (assignment.isPresent()) {
            List<String> imageUrlList = new ArrayList<>();

            int index = 1;
            for (MultipartFile multipartFile : multipartFileList) {
                String originalFilename = multipartFile.getOriginalFilename();
                String fileName = SecurityContextHolder.getContext().getAuthentication().getName() + "/" + assignmentId + "-" + UUID.randomUUID() + "-(" + index + ")" + originalFilename.substring(originalFilename.lastIndexOf(".")); // 파일명 지정 (format : 유저아이디/숙제ID-랜덤값-(순서).확장자)
                s3Service.upload(multipartFile, fileName);
                imageUrlList.add(fileName);
                index++;
            }

            Submit submit = Submit.builder()
                    .assignment(assignment.get())
                    .dateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                    .rate(0L)
                    .imageUrl(imageUrlList)
                    .build();

            submitRepository.save(submit);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_ASSIGNMENT.getMsg()));
        }
    }

    /* 숙제 인증피드 삭제 */
    public ResponseEntity<?> deleteSubmit (Long submitId) {

        /* Authorization - 이 숙제인증피드를 올린 학생만 접근 가능 */
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Long tuteeIdOfSubmit = submitRepository.GetTuteeIdOfSubmit(submitId);

        Optional<Submit> submit = submitRepository.findById(submitId);

        if ((tuteeIdOfSubmit == null && submit.isPresent()) || (tuteeIdOfSubmit != null && userId != tuteeIdOfSubmit)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
        }
        /* End */

        if (submit.isPresent()) {
            List<String> S3Urls = submit.get().getImageUrl();
            for (String url : S3Urls) {
                s3Service.delete(url); // S3에 업로드된 이미지들 삭제 먼저
            }
            submitRepository.deleteById(submitId); // 숙제 인증피드 삭제
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_SUBMIT.getMsg()));
        }
    }

    /* 숙제 인증피드 평가 */
    public ResponseEntity<?> evaluateSubmit (Long submitId, SubmitRequestDTO.Evaluate evaluateSubmit) {

        /* Authorization - 이 숙제인증피드의 숙제를 등록한 선생님만 접근 가능 */
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Long tutorIdOfSubmit = submitRepository.GetTutorIdOfSubmit(submitId);
        if (tutorIdOfSubmit != null && userId != tutorIdOfSubmit) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMsg(ResponseMsgList.NOT_AUTHORIZED.getMsg()));
        }
        /* End */

        Optional<Submit> submit = submitRepository.findById(submitId);
        if (submit.isPresent()) {
            Submit s = submit.get();
            s.setRate(evaluateSubmit.getRate());
            submitRepository.save(s);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NOT_EXIST_SUBMIT.getMsg()));
        }
    }
}
