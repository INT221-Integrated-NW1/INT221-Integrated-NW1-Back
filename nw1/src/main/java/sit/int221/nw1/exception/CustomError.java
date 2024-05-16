package sit.int221.nw1.exception;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomError {

    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String instance;

    // getters and setters
}