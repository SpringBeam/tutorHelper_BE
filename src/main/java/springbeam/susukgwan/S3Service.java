package springbeam.susukgwan;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springbeam.susukgwan.assignment.AssignmentRepository;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;
    private final AssignmentRepository assignmentRepository;

    /* 파일 업로드 */
    public String upload(MultipartFile multipartFile, String s3FileName) throws IOException {
        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(multipartFile.getInputStream().available());

        amazonS3.putObject(bucket, s3FileName, multipartFile.getInputStream(), objMeta);

        return URLDecoder.decode(amazonS3.getUrl(bucket, s3FileName).toString(), "utf-8"); // url에 한글&특수문자가 포함되어있을 경우 깨짐 방지
    }

    /* 파일 삭제 */
    public void delete (String keyName) {
        try {
            amazonS3.deleteObject(bucket, keyName);
        } catch (AmazonServiceException e) {
            log.error(e.toString());
        }
    }

    /* presigned URL 반환 */
    public String getPresignedURL (String keyName) {
        String preSignedURL = "";

        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Long fileAssignmentId = Long.parseLong(keyName.substring(keyName.indexOf("/")+1, keyName.indexOf("-")));

        List<Long> allUserIdOfAssignment = new ArrayList<>();
        allUserIdOfAssignment.add(assignmentRepository.GetTutorIdOfAssignment(fileAssignmentId));
        allUserIdOfAssignment.add(assignmentRepository.GetTuteeIdOfAssignment(fileAssignmentId));
        allUserIdOfAssignment.add(assignmentRepository.GetParentIdOfAssignment(fileAssignmentId));

        if (allUserIdOfAssignment.contains(userId)) { // 해당 수업의 선생님, 학생, 학부모 모두 접근가능
            Date expiration = new Date();
            Long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 2; // 만료기한 2분
            expiration.setTime(expTimeMillis);

            try {
                GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, keyName)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
                URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
                preSignedURL = url.toString();
            } catch (Exception e) {
                log.error(e.toString());
            }
        }

        return preSignedURL;
    }
}
