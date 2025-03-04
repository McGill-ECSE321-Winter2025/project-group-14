package ca.mcgill.ecse321.gamenight.model;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class GameReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Date datePosted;
    private int rating;
    private String comment;
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Player reviewer;
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    public GameReview() {

    }

    public GameReview(int rating, String comment, Player reviewer, Game game) {
        this.datePosted = new Date(System.currentTimeMillis());
        this.rating = rating;
        this.comment = comment;
        this.reviewer = reviewer;
        this.game = game;
    }

    public int getId() {
        return id;
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

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Player getReviewer() {
        return reviewer;
    }

    public Game getGame() {
        return game;
    }
}
