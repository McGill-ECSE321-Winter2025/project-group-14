package ca.mcgill.ecse321.gamenight.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Object does not exist in the system")
public class ObjectNotFoundException extends RuntimeException {

	public ObjectNotFoundException(String message) {
		super(message);
	}

    public ObjectNotFoundException(HttpStatus notFound, String string) {
        //TODO Auto-generated constructor stub
    }
}
