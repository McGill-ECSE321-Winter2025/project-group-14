package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEventNameException extends RuntimeException {

    private final String message;

    public InvalidEventNameException() {
        super("Event name cannot be null or empty.");
        this.message = "Event name cannot be null or empty.";
    }

    @Override
    public String getMessage() {
        return message;
    }
}
