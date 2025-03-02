package ca.mcgill.ecse321.gamenight.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ca.mcgill.ecse321.gamenight.dto.ErrorDto;

@ControllerAdvice
public class GameNightExceptionHandler {
    
    @ExceptionHandler(GameNightException.class)
	public ResponseEntity<ErrorDto> handleGameNightException(GameNightException e) {
		return new ResponseEntity<ErrorDto>(new ErrorDto(e.getMessage()), e.getStatus());
	}
}
