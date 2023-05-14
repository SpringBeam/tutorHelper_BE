package springbeam.susukgwan.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class SubmitRequestDTO {
    @Getter
    @Setter
    public static class Evaluate {
        @NotNull
        private Long rate;
    }
}
