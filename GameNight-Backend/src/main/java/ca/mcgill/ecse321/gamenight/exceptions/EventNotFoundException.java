package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EventNotFoundException extends RuntimeException {

    private final String message;

    public EventNotFoundException(int eventId) {
        super("No Event found with ID: " + eventId);
        this.message = "No Event found with ID: " + eventId;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
