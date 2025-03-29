package ca.mcgill.ecse321.gamenight.dto;

import java.util.Date;

public class GameReviewDto {

    private int reviewId;
    private int rating;
    private String comment;
    private int reviewerId;
    private int gameId;
    private String author;
    private Date datePosted;

    public GameReviewDto() {
    }

    public GameReviewDto(int reviewId, int rating, String comment, int reviewerId, int gameId, String author, Date date) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.comment = comment;
        this.reviewerId = reviewerId;
        this.gameId = gameId;
        this.author = author;
        this.datePosted = date;
    }

    public int getReviewId() {
        return reviewId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public int getReviewerId() {
        return reviewerId;
    }

    public int getGameId() {
        return gameId;
    }

    public String getAuthor() {
        return author;
    }

    public Date getDatePosted() {
        return datePosted;
    }
}