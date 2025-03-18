package ca.mcgill.ecse321.gamenight.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BorrowingRequestNotFoundException extends RuntimeException {
    private final String message;
    public BorrowingRequestNotFoundException(int requestId) {
        super("Borrowing request not found with ID: " + requestId);
        this.message = "Borrowing request not found with ID: " + requestId;
    }

    @Override
    public String getMessage(){
        return message;
    }
}
