package springbeam.susukgwan.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class AssignmentRequestDTO {

    @Getter
    @Setter
    public static class Create {
        @NotNull
        private Long tutoringId;
        @NotBlank
        private String body;
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate endDate;
//        private Long frequency;
        @NotNull
        private String amount; // 빈칸 가능
    }

    @Getter
    @Setter
    public static class Update {
        private String body;
        private LocalDate startDate;
        private LocalDate endDate;
        //        private Long frequency;
        private String amount;
    }
}
