//package sit.int221.nw1.exception;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.context.request.WebRequest;
//
//import java.time.LocalDateTime;
//
//@ControllerAdvice
//public class ExceptionControllerAdvice {
//    @ExceptionHandler(ItemNotFoundException.class)
//    public ResponseEntity<ErrorDetails> handleItemNotFoundException(ItemNotFoundException ex, WebRequest request) {
//        ErrorDetails errorDetails = new ErrorDetails();
//        errorDetails.setTimestamp(LocalDateTime.now());
//        errorDetails.setStatus(HttpStatus.NOT_FOUND.value());
//        errorDetails.setMessage(ex.getMessage());
//        errorDetails.setInstance(request.getDescription(false));
//        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
//    }
//}