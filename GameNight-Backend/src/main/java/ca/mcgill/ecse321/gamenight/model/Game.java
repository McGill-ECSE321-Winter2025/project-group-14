package ca.mcgill.ecse321.gamenight.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Game {

    @Id
    private String name;
    private String description;

    public Game(String name, String description) {
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
