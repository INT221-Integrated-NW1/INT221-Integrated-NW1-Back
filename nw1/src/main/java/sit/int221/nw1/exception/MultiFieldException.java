package sit.int221.nw1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
public class MultiFieldException extends RuntimeException {
    private List<FieldError> fieldErrors;


    public MultiFieldException(List<MultiFieldException.FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public static class FieldError {
        private String field;
        private String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}