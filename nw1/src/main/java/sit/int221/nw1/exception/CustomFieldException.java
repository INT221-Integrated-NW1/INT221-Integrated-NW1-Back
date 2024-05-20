package sit.int221.nw1.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CustomFieldException extends RuntimeException {
    private final String field;
    private final String message;

    public CustomFieldException(String field, String message) {
        super(message);
        this.field = field;
        this.message = message;
    }

    // Add constructor to convert FieldError to CustomFieldException
    public CustomFieldException(MultiFieldException.FieldError fieldError) {
        super(fieldError.getMessage());
        this.field = fieldError.getField();
        this.message = fieldError.getMessage();
    }
}