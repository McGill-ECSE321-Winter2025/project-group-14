package ca.mcgill.ecse321.gamenight.model;

import jakarta.persistence.Entity;

@Entity
public class Player extends AccountRole {
    private boolean isActive;

    public Person getPerson() {
        return super.getPerson();
    }
    

    public Player() {

    }

    public Player(Person person) {
        super(person);
    }
    

    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
