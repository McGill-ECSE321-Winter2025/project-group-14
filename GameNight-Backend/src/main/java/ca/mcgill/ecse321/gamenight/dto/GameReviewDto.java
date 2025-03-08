package ca.mcgill.ecse321.gamenight.dto;

public class GameReviewDto {

    private int reviewId;
    private int rating;
    private String comment;
    private int reviewerId;
    private int gameId;

    public GameReviewDto() {
    }

    public GameReviewDto(int reviewId, int rating, String comment, int reviewerId, int gameId) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.comment = comment;
        this.reviewerId = reviewerId;
        this.gameId = gameId;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(int reviewerId) {
        this.reviewerId = reviewerId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}