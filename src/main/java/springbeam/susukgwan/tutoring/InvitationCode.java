package springbeam.susukgwan.tutoring;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.user.Role;

@Entity
@Table(name = "invitation_code")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class InvitationCode {
    @Id
    private String code;

    private Long tutoringId;

    private Role role;  // 초대 역할 tutee or parent
}
