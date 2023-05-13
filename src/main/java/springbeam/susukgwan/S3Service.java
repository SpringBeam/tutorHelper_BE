package springbeam.susukgwan;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    /* 파일 업로드 */
    public String upload(MultipartFile multipartFile) throws IOException {
        String s3FileName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(multipartFile.getInputStream().available());

        amazonS3.putObject(bucket, s3FileName, multipartFile.getInputStream(), objMeta);

        return URLDecoder.decode(amazonS3.getUrl(bucket, s3FileName).toString(), "utf-8"); // url에 한글이 포함되어있을 경우 깨짐 방지
    }

    /* 파일 삭제 */
    public void delete (String fileUrl) {
        try {
            String keyName = fileUrl.substring(fileUrl.lastIndexOf("/"));
            keyName = keyName.substring(1);
            amazonS3.deleteObject(bucket, keyName);
        } catch (AmazonServiceException e) {
            log.error(e.toString());
        }
    }
}
