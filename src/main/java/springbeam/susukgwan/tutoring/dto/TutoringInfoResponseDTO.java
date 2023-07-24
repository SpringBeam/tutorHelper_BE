package springbeam.susukgwan.tutoring.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class TutoringInfoResponseDTO {
    @Getter
    @Setter
    @Builder
    public static class Tutor{
        private Long tutoringId;
        private String subject;
        private String tuteeName;
        private String profileImageUrl;
    }
    @Getter
    @Setter
    @Builder
    public static class Tutee{
        private Long tutoringId;
        private String subject;
        private String tutorName;
        private String profileImageUrl;
    }
    @Getter
    @Setter
    @Builder
    public static class Parent{
        private Long tutoringId;
        private String subject;
        private String tutorName;
        private String tuteeName;
        private String profileImageUrl;
    }
}
