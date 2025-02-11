package ca.mcgill.ecse321.gamenight.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
public class Player extends AccountRole{

    @OneToOne
    private Player reviewer;

    @OneToOne
    private Player sender;

    public Player(){
        
    }

    public Player getReviewer() {
        return reviewer;
    }

    public void setReviewer(Player reviewer) {
        this.reviewer = reviewer;
    }

    public Player getSender() {
        return sender;
    }

    public void setSender(Player sender) {
        this.sender = sender;
    }


}

