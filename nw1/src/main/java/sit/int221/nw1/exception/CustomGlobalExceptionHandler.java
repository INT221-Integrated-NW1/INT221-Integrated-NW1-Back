package sit.int221.nw1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
public class CustomGlobalExceptionHandler {
    //
//    @ExceptionHandler(ItemNotFoundException.class)
//    public ResponseEntity<CustomError> customHandleNotFound(Exception ex, WebRequest request) {
//
//        CustomError errors = new CustomError();
//        errors.setTimestamp(LocalDateTime.now());
//        errors.setMessage(ex.getMessage());
//        errors.setStatus(HttpStatus.NOT_FOUND.value());
//        errors.setInstance(request.getDescription(false).substring(4)); // remove "uri="
//
//        return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
//    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed. Check 'error' field for details.",
                request.getDescription(false)
        );

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorResponse.addValidationError(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


//    @ExceptionHandler(CustomFieldException.class)
//    public ResponseEntity<ErrorResponse> handleCustomFieldException(CustomFieldException ex) {
//        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation error. Check 'errors' field for details.", null);
//        errorResponse.addValidationError(ex.getField(), ex.getMessage());
//        return ResponseEntity.badRequest().body(errorResponse);
//    }


//    @ExceptionHandler(ResponseStatusException.class)
//    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, WebRequest webRequest) {
//        ErrorResponse errorResponse = new ErrorResponse(ex.getStatusCode().value(), ex.getMessage(), webRequest.getDescription(false));
//        return ResponseEntity.badRequest().body(errorResponse);
//    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Username or Password is incorrect", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}
//}
//    }
//    @ExceptionHandler(ResponseStatusException.class)
//    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", ex.getStatusCode().value());
//        response.put("message", ex.getReason());
//        response.put("error", ex.getStatusCode().toString()); // Replaced getReasonPhrase() with name()
//        return new ResponseEntity<>(response, ex.getStatusCode());
//    }
