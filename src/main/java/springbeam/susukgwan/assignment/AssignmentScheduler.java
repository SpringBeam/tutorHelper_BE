package springbeam.susukgwan.assignment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springbeam.susukgwan.fcm.PushService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class AssignmentScheduler {
    private final AssignmentRepository assignmentRepository;
    private final PushService pushService;

    @Scheduled(cron = "0 0 12 * * *") // 매일 오후 12시 0분 0초에 실행됨 "0 0 12 * * *" / 테스트용 10초마다 "0/10 * * * * *"
    public void checkDeadline() {
        log.info(String.valueOf(LocalDateTime.now()));
        List<Assignment> assignmentList = assignmentRepository.findByEndDateAndIsCompleted(LocalDate.now(), false); // 오늘 마감인것 중 미완료된 숙제들
        for (Assignment a : assignmentList) {
            pushService.assignmentDeadlineNotification(a);
        }
    }
}
