package ca.mcgill.ecse321.gamenight.model;

public class Player extends AccountRole{

    private Player reviewer;

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
