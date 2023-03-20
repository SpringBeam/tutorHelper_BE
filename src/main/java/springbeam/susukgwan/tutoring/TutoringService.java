package springbeam.susukgwan.tutoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.schedule.Time;
import springbeam.susukgwan.schedule.TimeRepository;
import springbeam.susukgwan.tutoring.dto.DeleteTutoringDTO;
import springbeam.susukgwan.tutoring.dto.RegisterTutoringDTO;
import springbeam.susukgwan.tutoring.dto.UpdateTutoringDTO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TutoringService {
    @Autowired
    private TutoringRepository tutoringRepository;
    @Autowired
    private TimeRepository timeRepository;

    public String registerTutoring (RegisterTutoringDTO registerTutoringDTO) {
        /* TODO 선생, 학생 및 과목 중복 확인 -> FAIL 설정 (나중에) */

        /* TODO완료 **현재 액세스토큰 확인하여 tutorId 가져오기!** (로그인 인증 구현 후) */
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);

        LocalDate startDate = LocalDate.parse(registerTutoringDTO.getStartDate()); // startDate parsing
        Tutoring newTutoring = Tutoring.builder().tutorId(tutorId).startDate(startDate)
                .subject(registerTutoringDTO.getSubject())
                        .build();
        newTutoring = tutoringRepository.save(newTutoring);

        String dayTimeString = registerTutoringDTO.getDayTime();
        String[] split = dayTimeString.split(",");
        Iterator<String> it = Arrays.stream(split).iterator();
        while (it.hasNext()) {
            String[] each = it.next().strip().split(" ");
            DayOfWeek dayOfWeek = DayOfWeek.of(Integer.valueOf(each[0]));
            LocalTime startTime = LocalTime.parse(each[1]);
            LocalTime endTime = LocalTime.parse(each[2]);
            Time regularTime = Time.builder()
                    .day(dayOfWeek)
                    .startTime(startTime)
                    .endTime(endTime)
                    .tutoring(newTutoring)
                            .build();
            timeRepository.save(regularTime);
        }
        return "SUCCESS";
    }

    public String updateTutoring(UpdateTutoringDTO updateTutoringDTO) {
        /* TODO완료 현재 액세스토큰 확인하여 본인 수업인지 확인 (로그인 인증 구현 후) */
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(updateTutoringDTO.getTutoringId(), tutorId);

        if (tutoringOptional.isPresent()) {
            Tutoring tutoring = tutoringOptional.get();
            tutoring.setSubject(updateTutoringDTO.getSubject());
            tutoring.setStartDate(LocalDate.parse(updateTutoringDTO.getStartDate()));
            tutoringRepository.save(tutoring);
            return "SUCCESS";
        }
        else {
            return "FAIL";
        }
    }

    public String deleteTutoring(DeleteTutoringDTO deleteTutoringDTO) {
        /* TODO완료 **현재 액세스토큰 확인하여 본인 수업인지 확인 (로그인 인증 구현 후) */
        String tutorIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        Long tutorId = Long.parseLong(tutorIdStr);
        Optional<Tutoring> tutoringOptional = tutoringRepository.findByIdAndTutorId(deleteTutoringDTO.getTutoringId(), tutorId);
        if (tutoringOptional.isPresent()) {
            tutoringRepository.delete(tutoringOptional.get());
            return "SUCCESS";
        }
        else {
            return "FAIL";
        }
    }
}
