package ca.mcgill.ecse321.gamenight.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class GameCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String description;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "gameowner_id")
    private GameOwner gameOwner;

    public GameCopy() {
    };

    public GameCopy(String description, Game game, GameOwner gameOwner) {
        this.description = description;
        this.game = game;
        this.gameOwner = gameOwner;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public GameCopy setDescription(String newDescription) {
        description = newDescription;
        return this;
    }

    public Game getGame() {
        return game;
    }

    public GameOwner getOwner() {
        return gameOwner;
    }
}