package sit.int221.nw1.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sit.int221.nw1.exception.CustomFieldException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(CustomFieldException.class)
    public ResponseEntity<Map<String, String>> handleCustomFieldException(CustomFieldException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("field", ex.getField());
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}