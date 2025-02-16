package ca.mcgill.ecse321.gamenight.model;

import jakarta.persistence.Entity;

@Entity
public class Player extends AccountRole {

    public Person getPerson() {
        return super.getPerson();
    }
    

    public Player() {

    }

    public Player(Person person) {
        super(person);
    }
}
