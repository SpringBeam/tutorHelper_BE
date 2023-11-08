package springbeam.susukgwan;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import springbeam.susukgwan.fcm.FCMService;
import springbeam.susukgwan.fcm.PushRequest;
import springbeam.susukgwan.schedule.ScheduleService;
import springbeam.susukgwan.schedule.dto.ScheduleInfoResponseDTO;
import springbeam.susukgwan.tutoring.Tutoring;
import springbeam.susukgwan.tutoring.TutoringRepository;
import springbeam.susukgwan.tutoring.TutoringService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@SpringBootTest
@ActiveProfiles("secret")
class SusukgwanApplicationTests {
	@Autowired
	private FCMService fcmService;
	@Autowired
	private ScheduleService scheduleService;
	@Autowired
	private TutoringService tutoringService;
	@Autowired
	private TutoringRepository tutoringRepository;
	private Logger logger = LoggerFactory.getLogger(SusukgwanApplicationTests.class);

	@Test
	void contextLoads() {
	}

	@Test
	void sendNotification() {
		PushRequest pushRequest = PushRequest.builder().title("아아 ㅋㅋㅋㅋㅋ ").topic("된다").body("하하하")
				.token("c5DcNXdrRSC5vHea45d6UO:APA91bGTNOkkipQKP8iyGTC9nsSjo1WqPw86wt0Y5A5hJqUigNV45FyybOQG9r2kE6dqJ6i2hlC-294DN6KF48uy2-RtDunGRC7IT7ckG3GjuskmYNpRulurxLtqR_RVNR60qRx_vK9z").build();
		try {
			fcmService.sendMessageToToken(pushRequest);
			return;
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			return;
		}
	}
	@Test
	@Transactional
	void scheduleDuplicate() {
		List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(1L);
		for (Tutoring tutoring: tutoringList) {
			logger.info(tutoring.getTimes().get(0).getStartTime().toString());
			logger.info(tutoring.getTutorId().toString());
		}
	}
	@Test
	public void deleteTutoring() {
		Optional<Tutoring> byId = tutoringRepository.findById(13L);
		if (byId.isPresent()) {
			tutoringRepository.delete(byId.get());
		}
		//test 결과 mysql workbench에서는 foreign key constraints 때문에 삭제가 안 되지만, JPA에서 삭제 시 제대로 tutoring과 time 모두 삭제됨.
	}
	@Test
	@Transactional
	public void testGetRegularSchedule() {
		Optional<Tutoring> tutoringOptional = tutoringRepository.findById(31L);
		Tutoring tutoring = tutoringOptional.get();
		List<ScheduleInfoResponseDTO> regularScheduleListByYearMonth = scheduleService.getRegularScheduleListByYearMonth(tutoring, LocalDate.of(2023, 8, 1));
		for (ScheduleInfoResponseDTO scheduleInfoResponseDTO: regularScheduleListByYearMonth) {
			logger.info(scheduleInfoResponseDTO.getDate());
		}

	}
//	@Test
//	public void deleteAllByTutorId() {
//		List<Tutoring> tutoringList = tutoringRepository.findAllByTutorId(12L);
//		tutoringRepository.deleteAll(tutoringList);
//	}

//	@Test
//	@Transactional
//	public void getTutoringList() {
//		Optional<Tutoring> byId = tutoringRepository.findById(15L);
//		if (byId.isPresent()) {
//			String str = tutoringService.makeDayTimeString(byId.get().getTimes());
//			logger.info(str);
//		}
//	}
}
