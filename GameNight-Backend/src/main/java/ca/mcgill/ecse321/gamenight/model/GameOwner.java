package ca.mcgill.ecse321.gamenight.model;

import jakarta.persistence.Entity;


@Entity
public class GameOwner extends AccountRole {

    private boolean isActive;
    public GameOwner() {}
   

    

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

}
