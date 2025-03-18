package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class GameNotFoundException extends RuntimeException {

    private final String message;

    public GameNotFoundException(int gameId) {
        super("No Game found with ID: " + gameId);
        this.message = "No Game found with ID: " + gameId;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
