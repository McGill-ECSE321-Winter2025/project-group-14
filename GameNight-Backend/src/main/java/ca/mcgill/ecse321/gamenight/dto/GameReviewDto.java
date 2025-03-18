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
}