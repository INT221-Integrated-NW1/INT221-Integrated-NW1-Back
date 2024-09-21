package sit.int221.nw1.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomFieldException extends RuntimeException {
    private final String field;
    private final String message;
    private HttpStatus status;

    public CustomFieldException(String field, String message) {
        super(message);
        this.field = field;
        this.message = message;
    }

}