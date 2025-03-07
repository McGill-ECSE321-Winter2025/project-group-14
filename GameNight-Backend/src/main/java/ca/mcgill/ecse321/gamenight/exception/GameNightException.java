package ca.mcgill.ecse321.gamenight.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public class GameNightException extends RuntimeException {
    
    private HttpStatus status;

	public GameNightException(@NonNull HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
