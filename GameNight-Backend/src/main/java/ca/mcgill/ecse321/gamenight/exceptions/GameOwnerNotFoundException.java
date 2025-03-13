package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;



@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class GameOwnerNotFoundException extends RuntimeException {
    private final String message;
    
    public GameOwnerNotFoundException(String gameCopyId) {
        super("Game copy not found with ID: " + gameCopyId);
        this.message = "Game copy not found with ID: " + gameCopyId;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
