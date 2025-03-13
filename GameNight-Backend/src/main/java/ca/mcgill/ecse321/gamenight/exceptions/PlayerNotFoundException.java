package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(int playerId) {
        super("Player not found with ID: " + playerId);
    }
}
