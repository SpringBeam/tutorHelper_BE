package springbeam.susukgwan.assignment.dto;

import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.S3Service;
import springbeam.susukgwan.assignment.Submit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SubmitResponseDTO { /* Submit 응답 DTO */
    private Long id;
    private LocalDateTime dateTime;
    private Long rate;
    private List<String> imageUrl = new ArrayList<>();

    public SubmitResponseDTO(Submit submit, S3Service s3Service) {
        this.id = submit.getId();
        this.dateTime = submit.getDateTime();
        this.rate = submit.getRate();

        for (String keyName : submit.getImageUrl()) {
            this.imageUrl.add(s3Service.getPresignedURL(keyName));
        }
    }
}
