package springbeam.susukgwan.tutoring;

import org.springframework.data.jpa.repository.JpaRepository;
import springbeam.susukgwan.user.Role;

import java.util.Optional;

public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {
    Optional<InvitationCode> findByCode(String code);
    Optional<InvitationCode> findByTutoringIdAndRole(Long tutoringId, Role role);
}
