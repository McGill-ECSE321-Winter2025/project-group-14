package ca.mcgill.ecse321.gamenight.model;

import java.sql.Date;

public class GameReview {

    private Date datePosted;
    private int rating;
    private String comment;
    private Player reviewer;

    public GameReview(int rating, String comment, Player reviewer) {
        this.datePosted = new Date(System.currentTimeMillis());
        this.rating = rating;
        this.comment = comment;
        this.reviewer = reviewer;
    }

    public Date getDatePosted() {
        return datePosted;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Player getReviewer() {
        return reviewer;
    }
}
