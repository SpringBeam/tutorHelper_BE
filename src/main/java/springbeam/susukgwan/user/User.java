package springbeam.susukgwan.user;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import springbeam.susukgwan.auth.RoleType;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType roleType;  // 권한

    @Column(nullable = false)
    private String socialId;

    @Column(nullable = false)
    private String name;

    // 프론트엔드 요청 임시 코드 socialId -> null 허용, userId -> null 허용. but,
    private String userId;
    // 암호화되어 저장됨.
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;  // 선생, 학생, 학부모 역할 중 하나

    private LocalDateTime createdAt;
}
