package ca.mcgill.ecse321.gamenight.dto;

import ca.mcgill.ecse321.gamenight.model.Game;

public class GameResponseDto {

    private int id;
    private String name;
    private String description;

    @SuppressWarnings("unused")
    private GameResponseDto() {
    }

    public GameResponseDto(Game model) {
        id = model.getId();
        name = model.getName();
        description = model.getDescription();
    }

    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
