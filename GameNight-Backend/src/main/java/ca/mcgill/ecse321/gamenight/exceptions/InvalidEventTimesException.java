package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEventTimesException extends RuntimeException {

    private final String message;

    public InvalidEventTimesException() {
        super("Event end time cannot be before the start time.");
        this.message = "Event end time cannot be before the start time.";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
