package ca.mcgill.ecse321.gamenight.dto;

import ca.mcgill.ecse321.gamenight.model.GameCopy;

public class GameCopyResponseDto {
    
    private int id;
    private GameResponseDto game;
    private String description;

    @SuppressWarnings("unused")
    private GameCopyResponseDto() {
    }

    public GameCopyResponseDto(GameCopy gameCopy) {
        this.id = gameCopy.getId();
        this.description = gameCopy.getDescription();
        this.game = new GameResponseDto(gameCopy.getGame());
    }

    public int getId() {
        return id;
    }
    
    public GameResponseDto getGame() {
        return game;
    }

    public String getDescription() {
        return description;
    }
}
