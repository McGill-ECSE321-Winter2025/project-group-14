package ca.mcgill.ecse321.gamenight.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AuthRequestDto {
    public AuthRequestDto() {
    }

    private String emailAdress;

    private String password;

    private String name;
}
