package backend.courseshare.dev;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class DevExceptionLogger {

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> handleAll(Throwable ex) {
        ex.printStackTrace();

        if (ex instanceof ResponseStatusException rse) {
            // preserve the status and message for ResponseStatusException
            return ResponseEntity.status(rse.getStatusCode())
                    .body(Map.of("error", ex.getClass().getName(), "message", rse.getReason()));
        }

        // fallback: internal server error for unexpected exceptions
        return ResponseEntity.status(500).body(Map.of("error", ex.getClass().getName(), "message", ex.getMessage()));
    }
}
