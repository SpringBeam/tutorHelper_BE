package springbeam.susukgwan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SusukgwanApplication {

	public static void main(String[] args) {
		SpringApplication.run(SusukgwanApplication.class, args);
	}

}
