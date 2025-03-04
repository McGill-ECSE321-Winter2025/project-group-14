package ca.mcgill.ecse321.gamenight.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.GameReviewRepository;

@Service
public class GameReviewService {

    @Autowired
    private GameReviewRepository gameReviewRepository;

    @Transactional
    public GameReview submitReview(int rating, String comment, Player reviewer, Game game) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }
        if (reviewer == null) {
            throw new IllegalArgumentException("Reviewer cannot be null.");
        }
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }

        GameReview review = new GameReview(rating, comment, reviewer, game);
        return gameReviewRepository.save(review);
    }

    @Transactional
    public boolean deleteReview(int reviewId) {
        if (gameReviewRepository.existsById(reviewId)) {
            gameReviewRepository.deleteById(reviewId);
            return true;
        }
        return false;
    }

    @Transactional
    public void deleteReviewsForGame(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        gameReviewRepository.deleteByGame(game);
    }

    @Transactional
    public GameReview updateReview(int reviewId, int rating, String comment) {
        GameReview review = gameReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty.");
        }

        review.setRating(rating);
        review.setComment(comment);
        return gameReviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public GameReview getReviewById(int reviewId) {
        return gameReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found."));
    }

    @Transactional(readOnly = true)
    public double getAverageRatingForGame(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        List<GameReview> reviews = gameReviewRepository.findByGame(game);
        if (reviews.isEmpty()) {
            return 0.0; // No reviews yet
        }
        return reviews.stream()
                .mapToInt(GameReview::getRating)
                .average()
                .orElse(0.0);
    }

    @Transactional
    public List<GameReview> getReviewsForGame(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        return gameReviewRepository.findByGameOrderByDatePostedDesc(game);
    }

    @Transactional
    public List<GameReview> getReviewsByUser(Player reviewer) {
        if (reviewer == null) {
            throw new IllegalArgumentException("Reviewer cannot be null.");
        }
        return gameReviewRepository.findByReviewer(reviewer);
    }

    @Transactional(readOnly = true)
    public List<GameReview> getReviewsSortedByRating(Game game, boolean ascending) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        if (ascending) {
            return gameReviewRepository.findByGameOrderByRatingAsc(game);
        } else {
            return gameReviewRepository.findByGameOrderByRatingDesc(game);
        }
    }
}