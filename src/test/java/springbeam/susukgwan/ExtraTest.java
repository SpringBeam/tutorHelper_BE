package springbeam.susukgwan;

import org.junit.jupiter.api.Test;

import java.util.Random;

public class ExtraTest {
    @Test
    public void randomStringTest() {
        int leftLimit = 48; // '0'
        int rightLimit = 122; // 'z'
        int length = 8;
        Random random = new Random();

        String randomStr = random.ints(leftLimit, rightLimit)
                .filter(i -> (i<=57 || i>=65) && (i<=90 || i>=97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        System.out.println("randomStr = " + randomStr);
    }
}
