package ca.mcgill.ecse321.gamenight.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Information is missing for this action")
public class MissingFieldsException extends RuntimeException {

    public MissingFieldsException(String message) {
        super(message);
    }
}
