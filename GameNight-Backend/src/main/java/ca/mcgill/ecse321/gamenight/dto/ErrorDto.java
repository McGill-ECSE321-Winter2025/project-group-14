package ca.mcgill.ecse321.gamenight.dto;

import java.util.List;

public class ErrorDto {
    
	private List<String> errors;

	@SuppressWarnings("unused")
	private ErrorDto() {
	}

	public ErrorDto(List<String> errors) {
		this.errors = errors;
	}

	public ErrorDto(String error) {
		this.errors = List.of(error);
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
}
