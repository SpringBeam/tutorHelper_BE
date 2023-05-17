package springbeam.susukgwan.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.ResponseMsg;
import springbeam.susukgwan.ResponseMsgList;
import springbeam.susukgwan.schedule.dto.ScheduleDTO;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ScheduleService {
    @Autowired
    private IrregularRepository irregularRepository;
    @Autowired
    private CancellationRepository cancellationRepository;
    @Autowired
    private TutoringRepository tutoringRepository;

    public ResponseEntity<?> newIrregularSchedule(ScheduleDTO scheduleDTO) {
        // Check whether the request user actually has this tutoring.
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(scheduleDTO.getTutoringId(), tutorId);
        if (tutoringOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMsg(ResponseMsgList.NO_SUCH_TUTORING.getMsg()));
        }

        List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(tutorId);
        // tutoringList.stream().filter(tutoring -> {tutoring.})


        // 모든 수업의 정규시간과 겹치는지 확인 (다만, 그 시간에 cancellation이 있으면 겹치지 않는 것임.)
        // 모든 비정규시간과 겹치는지 확인
        // 겹치지 않으면 새 일정 저장 후 200 ok
        return ResponseEntity.ok().build();
    }

    /*
    private boolean isAvailableBetween(ScheduleDTO scheduleDTO) {
        if (!isAvailableBetween(scheduleDTO)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseMsg(""))
        }
    }
    // 한 날짜의 시작시간~종료시간 사이에 다른 일정이 존재하는지 확인하는 함수
    */
}
