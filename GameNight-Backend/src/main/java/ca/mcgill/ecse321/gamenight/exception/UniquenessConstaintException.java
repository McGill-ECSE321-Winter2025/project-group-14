package ca.mcgill.ecse321.gamenight.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Input must be unique")
public class UniquenessConstaintException extends RuntimeException {

    public UniquenessConstaintException(String message) {
        super(message);
    }
}
