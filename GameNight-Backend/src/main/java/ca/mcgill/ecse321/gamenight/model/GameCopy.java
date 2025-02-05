package ca.mcgill.ecse321.gamenight.model;

public class GameCopy {

    private String description;
    private Game game;
    private GameOwner gameOwner;

    public GameCopy(String description, Game game, GameOwner gameOwner) {
        this.description = description;
        this.game = game;
        this.gameOwner = gameOwner;
    }

    public String getDescription() {
        return description;
    }

    public String setDescription(String newDescription) {
        description = newDescription;
        return description;
    }

    public Game getGame() {
        return game;
    }

    public GameOwner getOwner() {
        return gameOwner;
    }
}
