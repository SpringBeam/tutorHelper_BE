package springbeam.susukgwan.tutoring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springbeam.susukgwan.user.Role;

import java.util.Optional;

@Repository
public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {
    Optional<InvitationCode> findByCode(String code);
    Optional<InvitationCode> findByTutoringIdAndRole(Long tutoringId, Role role);
}
