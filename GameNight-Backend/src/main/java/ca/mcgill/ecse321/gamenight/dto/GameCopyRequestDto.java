package ca.mcgill.ecse321.gamenight.dto;

public class GameCopyRequestDto {

    private int gameId;
    private int ownerId;
    private String description;

    @SuppressWarnings("unused")
    private GameCopyRequestDto() {
    }

    public GameCopyRequestDto(int gameId, int ownerId, String description) {
        this.gameId = gameId;
        this.ownerId = ownerId;
        this.description = description;
    }

    public int getGameId() {
        return gameId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getDescription() {
        return description;
    }
}
