package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;



@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ReqGameCopyNotFoundException extends RuntimeException {

    private final String message;
    
    public ReqGameCopyNotFoundException(String gameCopyId) {
        super("No active borrowing request found for Game Copy ID: " + gameCopyId);
        this.message = "No active borrowing request found for Game Copy ID: " + gameCopyId;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
