package ca.mcgill.ecse321.gamenight.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ca.mcgill.ecse321.gamenight.dto.ErrorDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GameNightExceptionHandler {
    
	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ErrorDto> handleForbiddenException(ForbiddenException e) {
		return new ResponseEntity<ErrorDto>(new ErrorDto(e.getMessage()), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<ErrorDto> handleInvalidInputException(InvalidInputException e) {
		return new ResponseEntity<ErrorDto>(new ErrorDto(e.getMessage()), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ErrorDto> handleUnauthorizedException(UnauthorizedException e) {
		return new ResponseEntity<ErrorDto>(new ErrorDto(e.getMessage()), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UniquenessConstaintException.class)
	public ResponseEntity<ErrorDto> handleUniquenessConstaintException(UniquenessConstaintException e) {
		return new ResponseEntity<ErrorDto>(new ErrorDto(e.getMessage()), HttpStatus.CONFLICT);
	}

    @ExceptionHandler(ObjectNotFoundException.class)
	public ResponseEntity<ErrorDto> handleObjectNotFoundException(ObjectNotFoundException e) {
		return new ResponseEntity<ErrorDto>(new ErrorDto(e.getMessage()), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MissingFieldsException.class)
	public ResponseEntity<ErrorDto> handleGameNightException(MissingFieldsException e) {
		return new ResponseEntity<ErrorDto>(new ErrorDto(e.getMessage()), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorDto> handleConstraintViolationException(ConstraintViolationException e) {
		List<String> errors = new ArrayList<>();
		for (ConstraintViolation<?> cv : e.getConstraintViolations()) {
			errors.add(cv.getMessage());
		}
		return new ResponseEntity<ErrorDto>(new ErrorDto(errors), HttpStatus.BAD_REQUEST);
	}
}

