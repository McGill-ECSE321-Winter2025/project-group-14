package ca.mcgill.ecse321.gamenight.dto;

import ca.mcgill.ecse321.gamenight.model.GameCopy;

public class GameCopyResponseDto {
    
    private int id;
    private GameResponseDto game;
    private String description;
    private String gameOwnerName;
    private int personId;

    @SuppressWarnings("unused")
    private GameCopyResponseDto() {
    }

    public GameCopyResponseDto(GameCopy gameCopy) {
        this.id = gameCopy.getId();
        this.description = gameCopy.getDescription();
        this.gameOwnerName = gameCopy.getOwner().getPerson().getName();
        this.personId = gameCopy.getOwner().getId();
        this.game = new GameResponseDto(gameCopy.getGame());
    }

    public int getId() {
        return id;
    }

    public String getGameOwnerName() {
        return gameOwnerName;
    }

    public int getPersonId() {
        return personId;
    }
    
    public GameResponseDto getGame() {
        return game;
    }

    public String getDescription() {
        return description;
    }
}
