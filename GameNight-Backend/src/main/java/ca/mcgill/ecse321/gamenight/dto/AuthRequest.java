package ca.mcgill.ecse321.gamenight.dto;

import lombok.Data;

@Data
public class AuthRequest {

    private String emailAdress;

    private String password;

    private String name;
}
