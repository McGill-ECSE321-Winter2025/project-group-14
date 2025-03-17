package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RegistrationNotFoundException extends RuntimeException {

    private final String message;

    public RegistrationNotFoundException(int eventId, int playerId) {
        super("No registration found for event " + eventId + " and player " + playerId);
        this.message = "No registration found for event " + eventId + " and player " + playerId;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
