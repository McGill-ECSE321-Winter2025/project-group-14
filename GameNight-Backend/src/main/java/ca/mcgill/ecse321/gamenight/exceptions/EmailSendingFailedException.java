package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class EmailSendingFailedException extends RuntimeException {
    private final String message;

    public EmailSendingFailedException() {
        super("Failed to send email");
        this.message = "Failed to send email";
    }

    public EmailSendingFailedException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
