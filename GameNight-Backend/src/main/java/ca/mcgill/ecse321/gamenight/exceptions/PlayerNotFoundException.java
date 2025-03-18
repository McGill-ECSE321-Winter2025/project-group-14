package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PlayerNotFoundException extends RuntimeException {

    private final String message;

    public PlayerNotFoundException(int playerId) {
        super("Player not found with ID: " + playerId);
        this.message = "Player not found with ID: " + playerId;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
