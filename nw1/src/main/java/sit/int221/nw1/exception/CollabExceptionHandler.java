package sit.int221.nw1.exception;

public class CollabExceptionHandler {
    public static class CollaboratorNotFoundException extends RuntimeException {
        public CollaboratorNotFoundException(String message) {
            super(message);
        }
    }

    public static class ForbiddenOperationException extends RuntimeException {
        public ForbiddenOperationException(String message) {
            super(message);
        }
    }
}
