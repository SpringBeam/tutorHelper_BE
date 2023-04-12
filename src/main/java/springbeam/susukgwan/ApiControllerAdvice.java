package springbeam.susukgwan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

    private final MessageSource messageSource;

    public ApiControllerAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /* Validation 예외처리 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors()
                .forEach(c -> {
                    errors.put(((FieldError) c).getField(), getErrorMessage(c));
                    log.error(c.toString());
                });
        return ResponseEntity.badRequest().body(errors);
    }

    /* TypeMismatch 예외처리 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatchExceptions(HttpMessageNotReadableException ex){
        Map<String, String> errors = new HashMap<>();

        Pattern errorFieldPattern = Pattern.compile("\\[[\"](.*?)[\"]\\]");
        Matcher errorFieldMatcher = errorFieldPattern.matcher(ex.getCause().getMessage());
        String errorField = errorFieldMatcher.find() ? errorFieldMatcher.group(1) : "FAIL";

        Pattern rightTypePattern = Pattern.compile("[`](.*?)[`]");
        Matcher rightTypeMatcher = rightTypePattern.matcher(ex.getMessage());
        String rightType = rightTypeMatcher.find() ? rightTypeMatcher.group(1) : "?";

        String errorMessage = messageSource.getMessage("typeMismatch", new Object[] {rightType}, Locale.KOREA);
        errors.put(errorField, errorMessage);

        log.error(ex.toString());

        return ResponseEntity.badRequest().body(errors);
    }

    /* errors.properties 에서 커스텀 에러메세지 가져옴 */
    private String getErrorMessage(ObjectError error) {
        String[] codes = error.getCodes();
        for (String code : codes) {
            try {
                return messageSource.getMessage(code, error.getArguments(), Locale.KOREA);
            } catch (NoSuchMessageException ignored) {}
        }
        return error.getDefaultMessage();
    }
}
