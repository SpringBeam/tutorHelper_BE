package springbeam.susukgwan;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import springbeam.susukgwan.fcm.FCMService;
import springbeam.susukgwan.fcm.PushRequest;

import java.util.Random;

@SpringBootTest
@ActiveProfiles("secret")
class SusukgwanApplicationTests {
	@Autowired
	private FCMService fcmService;
	private final static Logger LOG = LoggerFactory.getLogger(SusukgwanApplicationTests.class);

	@Test
	void contextLoads() {
	}

	@Test
	void sendNotification() {
		PushRequest pushRequest = PushRequest.builder().title("test").topic("test").body("학교 종이 쌩쌩쌩").token("A").build();
		try {
			fcmService.sendMessageToToken(pushRequest);
			return;
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
			return;
		}
	}

}
