package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class GameOwnerNotFoundException extends RuntimeException {
    public GameOwnerNotFoundException(String message) {
        super(message);
    }
}
