package ca.mcgill.ecse321.gamenight.dto;

public class GameRequestDto {
   
    private String name;
    private String description;

    @SuppressWarnings("unused")
    private GameRequestDto() {
    }

    public GameRequestDto(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
