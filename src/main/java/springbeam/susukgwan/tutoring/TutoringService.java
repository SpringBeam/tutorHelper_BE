package springbeam.susukgwan.tutoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springbeam.susukgwan.schedule.Time;
import springbeam.susukgwan.schedule.TimeRepository;
import springbeam.susukgwan.subject.Subject;
import springbeam.susukgwan.subject.SubjectRepository;
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
    @Autowired
    private SubjectRepository subjectRepository;

    public String registerTutoring (RegisterTutoringDTO registerTutoringDTO) {
        /* TODO 선생, 학생 및 과목 중복 확인 -> FAIL 설정 (나중에) */
        /* TODO **현재 액세스토큰 확인하여 tutorId 가져오기!** (로그인 인증 구현 후) */
        Long tutorId = 0L;

        Subject subject = findSubject(registerTutoringDTO.getSubject(), tutorId);

        LocalDate startDate = LocalDate.parse(registerTutoringDTO.getStartDate()); // startDate parsing
        Tutoring newTutoring = Tutoring.builder().tutorId(tutorId).startDate(startDate)
                        .subject(subject) // 과목 매핑
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
        /* TODO **현재 액세스토큰 확인하여 본인 수업인지 확인 (로그인 인증 구현 후) */
        Optional<Tutoring> tutoringOptional = tutoringRepository.findById(updateTutoringDTO.getTutoringId());
        Long tutorId = 0L;
        if (tutoringOptional.isPresent()) {
            Tutoring tutoring = tutoringOptional.get();
            Subject subject = findSubject(updateTutoringDTO.getSubject(), tutorId);
            tutoring.setSubject(subject);
            tutoring.setStartDate(LocalDate.parse(updateTutoringDTO.getStartDate()));
            tutoringRepository.save(tutoring);
            return "SUCCESS";
        }
        else {
            return "FAIL";
        }
    }

    public String deleteTutoring(DeleteTutoringDTO deleteTutoringDTO) {
        /* TODO **현재 액세스토큰 확인하여 본인 수업인지 확인 (로그인 인증 구현 후) */
        Optional<Tutoring> tutoringOptional = tutoringRepository.findById(deleteTutoringDTO.getTutoringId());
        if (tutoringOptional.isPresent()) {
            tutoringRepository.delete(tutoringOptional.get());
            return "SUCCESS";
        }
        else {
            return "FAIL";
        }
    }

    /* Subject : 기존에 있는거면 그거 반환, 없으면 새로 저장해서 반환 */
    public Subject findSubject(String subjectName, Long tutorId) {
        Subject subject = new Subject();
        Optional<Subject> duplicateSubject = subjectRepository.findByNameAndUserId(subjectName, tutorId); // 입력한 과목명, 튜터아이디로 이미 등록된 과목인지 확인
        if (duplicateSubject.isEmpty()) { // 기존에 없었으면 과목 새로 만듦
            subject = Subject.builder()
                    .name(subjectName)
                    .userId(tutorId)
                    .build();
            subjectRepository.save(subject); // subject 저장
        } else { // 기존에 있었으면 있던거 가져옴
            subject = duplicateSubject.get();
        }
        return subject;
    }
}
