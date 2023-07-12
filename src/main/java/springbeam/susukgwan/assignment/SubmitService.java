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
import springbeam.susukgwan.fcm.PushService;

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
    private final PushService pushService;

    /* 숙제 인증피드 등록 */
    public ResponseEntity<?> submitFiles (Long assignmentId, List<MultipartFile> multipartFileList) throws IOException {
        // 업로드 파일 존재여부 확인
        for (MultipartFile multipartFile : multipartFileList) {
            if (multipartFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.NO_FILE.getMsg()));
            }
        }

        if (multipartFileList.size() > 3) { // 인증사진은 최대 3개
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMsg(ResponseMsgList.SUBMIT_CONSTRAINTS.getMsg()));
        }

        Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);
        Assignment assignment = assignmentOptional.get();
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
                .assignment(assignment)
                .dateTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .rate(0L)
                .imageUrl(imageUrlList)
                .build();

        assignment.setCount(assignment.getCount() + 1); // 제출횟수 1 증가
        if (assignment.getCount() >= assignment.getGoalCount()) { // 제출횟수 모두 채우면 완료(true)
            assignment.setIsCompleted(true);
        }
        assignmentRepository.save(assignment);

        submitRepository.save(submit);
        pushService.assignmentSubmitNotification(assignment, Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));
        return ResponseEntity.ok().build();
    }

    /* 숙제 인증피드 삭제 */
    public ResponseEntity<?> deleteSubmit (Long submitId) {
        Optional<Submit> submit = submitRepository.findById(submitId);
        List<String> S3Urls = submit.get().getImageUrl();
        for (String url : S3Urls) {
            s3Service.delete(url); // S3에 업로드된 이미지들 삭제 먼저
        }
        submitRepository.deleteById(submitId); // 숙제 인증피드 삭제

        Assignment assignment = submit.get().getAssignment();
        assignment.setCount(assignment.getCount() - 1); // 제출횟수 1 감소
        if (assignment.getCount() < assignment.getGoalCount()) { // 제출횟수 모두 못채우면 다시 미완료로 (false)
            assignment.setIsCompleted(false);
        }
        assignmentRepository.save(assignment);

        return ResponseEntity.ok().build();
    }

    /* 숙제 인증피드 평가 */
    public ResponseEntity<?> evaluateSubmit (Long submitId, SubmitRequestDTO.Evaluate evaluateSubmit) {
        Optional<Submit> submit = submitRepository.findById(submitId);
        Submit s = submit.get();
        s.setRate(evaluateSubmit.getRate());
        submitRepository.save(s);
        return ResponseEntity.ok().build();
    }
}
