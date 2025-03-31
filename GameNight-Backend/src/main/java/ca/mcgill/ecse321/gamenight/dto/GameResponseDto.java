package ca.mcgill.ecse321.gamenight.dto;

import ca.mcgill.ecse321.gamenight.model.Game;

public class GameResponseDto {

    private int id;
    private String name;
    private String description;
    private Double rating;

    @SuppressWarnings("unused")
    private GameResponseDto() {
    }

    public GameResponseDto(Game model, Double rating) {
        id = model.getId();
        name = model.getName();
        description = model.getDescription();
        this.rating = rating;
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

    public Double getRating() {
        return rating;
    }
}
