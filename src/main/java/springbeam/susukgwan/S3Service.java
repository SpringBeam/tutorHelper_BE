package springbeam.susukgwan;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springbeam.susukgwan.assignment.AssignmentRepository;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

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
    private final TutoringRepository tutoringRepository;

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

    /* public 객체 url 반환 */
    public String getPublicURL (String keyName) {
        return amazonS3.getUrl(bucket, keyName).toString();
    }

    /* public 객체 업로드 */
    public String uploadPublic(MultipartFile multipartFile, String s3FileName) throws IOException {
        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(multipartFile.getInputStream().available());
        amazonS3.putObject(
                new PutObjectRequest(bucket, s3FileName, multipartFile.getInputStream(), objMeta)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );
        return URLDecoder.decode(amazonS3.getUrl(bucket, s3FileName).toString(), "utf-8"); // url에 한글&특수문자가 포함되어있을 경우 깨짐 방지
    }

    /* 프로필사진 가져오기 */
    public String getProfilePresignedURL (String keyName) {
        String preSignedURL = "";

        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        Long fileUserId = Long.parseLong(keyName.substring(keyName.indexOf("-")+1, keyName.indexOf(".")));

        List<Long> allUserId = new ArrayList<>();
        List<Tutoring> tutoringByTutor = tutoringRepository.findAllByTutorId(fileUserId);
        List<Tutoring> tutoringByTutee = tutoringRepository.findAllByTuteeId(fileUserId);
        List<Tutoring> tutoringByParent = tutoringRepository.findAllByParentId(fileUserId);

        for (Tutoring t : tutoringByTutor) {
            allUserId.add(t.getTuteeId());
            allUserId.add(t.getParentId());
        }

        for (Tutoring t : tutoringByTutee) {
            allUserId.add(t.getTutorId());
            allUserId.add(t.getParentId());
        }

        for (Tutoring t : tutoringByParent) {
            allUserId.add(t.getTutorId());
            allUserId.add(t.getTuteeId());
        }

        if (allUserId.contains(userId) || userId == fileUserId) { // 접근권한이 있는 사람이면
            Date expiration = new Date();
            Long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 60 * 24; // 만료기한 하루
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
