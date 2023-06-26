package springbeam.susukgwan.fcm;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PushRepository extends JpaRepository<Push, Long> {
    List<Push> findAllByReceiverId(Long receiverId);
    List<Push> findAllByReceiverIdAndIsRead(Long receiverId, boolean isRead);
}
