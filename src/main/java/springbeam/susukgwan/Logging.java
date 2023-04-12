package springbeam.susukgwan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
public class Logging {
    public static ResponseEntity<?> PrintLogAndReturnResponseEntity (ResponseEntity responseEntity) {
        String location = Thread.currentThread().getStackTrace()[2].getClassName() + "." + Thread.currentThread().getStackTrace()[2].getMethodName();
        if (responseEntity.hasBody()) {
            if (responseEntity.getBody().getClass() == ResponseMsg.class) {
                log.info("location={}, status code={}, body={}", location, responseEntity.getStatusCode(), ((ResponseMsg) responseEntity.getBody()).getMsg());
            } else {
                log.info("location={}, status code={}, body={}", location, responseEntity.getStatusCode(), responseEntity.getBody());
            }

        } else {
            log.info("location={}, status code={}", location, responseEntity.getStatusCode());
        }
        return responseEntity;
    }
}
