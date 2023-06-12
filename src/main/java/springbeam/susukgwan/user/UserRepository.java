package springbeam.susukgwan.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndSocialId(Provider provider, String socialId);
    Optional<User> findByUserId(String userId);
}
