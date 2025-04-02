package ca.mcgill.ecse321.gamenight.dto;

import ca.mcgill.ecse321.gamenight.model.Game;

public class GameResponseDto {
    private int id;
    private String name;
    private String description;
    private String imageUrl;
    private Double rating;

    @SuppressWarnings("unused")
    private GameResponseDto() {
    }

    public GameResponseDto(Game model, Double rating) {
        id = model.getId();
        name = model.getName();
        description = model.getDescription();
        this.rating = rating;
        this.imageUrl = model.getImagePath() != null ? "/api/games/" + model.getId() + "/image" : null;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public Double getRating() {
        return rating;
    }
}
