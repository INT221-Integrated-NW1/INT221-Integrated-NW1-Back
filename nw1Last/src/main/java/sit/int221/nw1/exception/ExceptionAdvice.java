package sit.int221.nw1.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("field", fieldName);
                    errorMap.put("message", errorMessage);
                    return errorMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("message", "Validation error. Check 'errors' field for details.");
        errorResponse.put("errors", errors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(CustomFieldException.class)
    public ResponseEntity<Map<String, Object>> handleCustomFieldException(CustomFieldException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("field", ex.getField());
        error.put("message", ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("message", "Validation error. Check 'errors' field for details.");
        errorResponse.put("errors", List.of(error));

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MultiFieldException.class)
    public ResponseEntity<Map<String, Object>> handleMultiFieldException(MultiFieldException ex) {
        List<Map<String, String>> errors = ex.getFieldErrors()
                .stream()
                .map(error -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put("field", error.getField());
                    errorMap.put("message", error.getMessage());
                    return errorMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("message", "Validation error. Check 'errors' field for details.");
        errorResponse.put("errors", errors);

        return ResponseEntity.badRequest().body(errorResponse);
    }
}