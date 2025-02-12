package ca.mcgill.ecse321.gamenight.model;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
public class GameOwner extends AccountRole {

    private boolean isActive;

    // @OneToMany(mappedBy = "gameOwner", cascade = CascadeType.ALL)
    // private Set<GameCopy> ownedGames;

    public GameOwner() {

    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

}
