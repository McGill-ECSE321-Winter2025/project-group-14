package ca.mcgill.ecse321.gamenight.model;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Player extends AccountRole {

    // @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    // private Set<Registration> registrations;

    // @OneToOne
    // private Player reviewer;

    // @OneToOne
    // private Player sender;

    public Player() {

    }

    // public Player getReviewer() {
    // return reviewer;
    // }

    // public void setReviewer(Player reviewer) {
    // this.reviewer = reviewer;
    // }

    // public Player getSender() {
    // return sender;
    // }

    // public void setSender(Player sender) {
    // this.sender = sender;
    // }

}
