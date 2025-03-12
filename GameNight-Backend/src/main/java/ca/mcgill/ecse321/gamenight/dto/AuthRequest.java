package ca.mcgill.ecse321.gamenight.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AuthRequest {
    public AuthRequest() {
    }

    private String emailAdress;

    private String password;

    private String name;
}
