package springbeam.susukgwan.assignment.dto;

import lombok.Getter;
import lombok.Setter;
import springbeam.susukgwan.assignment.Submit;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SubmitResponseDTO { /* Submit 응답 DTO */
    private Long id;
    private LocalDateTime dateTime;
    private Long rate;
    private List<String> imageUrl;

    public SubmitResponseDTO(Submit submit) {
        this.id = submit.getId();
        this.dateTime = submit.getDateTime();
        this.rate = submit.getRate();
        this.imageUrl = submit.getImageUrl();
    }
}
