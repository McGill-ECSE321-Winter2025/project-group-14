package ca.mcgill.ecse321.gamenight.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gamenight.exception.MissingFieldsException;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.exception.InvalidInputException;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.GameReviewRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;

@Service
public class GameReviewService {

    @Autowired
    private GameReviewRepository gameReviewRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;

    @Transactional
    public GameReview submitReview(int rating, String comment, Player reviewer, Game game) {
        if (rating < 1 || rating > 5) {
            throw new InvalidInputException("Rating must be between 1 and 5.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new MissingFieldsException("Comment cannot be empty.");
        }
        if (reviewer == null) {
            throw new MissingFieldsException("Reviewer cannot be null.");
        }
        if (game == null) {
            throw new MissingFieldsException("Game cannot be null.");
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
    public GameReview updateReview(int reviewId, int rating, String comment) {
        GameReview review = gameReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ObjectNotFoundException("Review not found."));

        if (rating < 1 || rating > 5) {
            throw new InvalidInputException("Rating must be between 1 and 5.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new MissingFieldsException("Comment cannot be empty.");
        }

        review.setRating(rating);
        review.setComment(comment);
        return gameReviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public GameReview getReviewById(int reviewId) {
        return gameReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ObjectNotFoundException("Review not found."));
    }

    @Transactional(readOnly = true)
    public double getAverageRatingForGame(Game game) {
        if (game == null) {
            throw new MissingFieldsException("Game cannot be null.");
        }
        if (!gameRepository.existsById(game.getId())) {
            throw new ObjectNotFoundException("Game not found.");
        }
        List<GameReview> reviews = gameReviewRepository.findByGame(game);
        if (reviews.isEmpty()) {
            return -1; // No reviews yet
        }
        return reviews.stream()
                .mapToInt(GameReview::getRating)
                .average()
                .orElse(0.0);
    }

    @Transactional
    public List<GameReview> getReviewsForGame(Game game) {
        if (game == null) {
            throw new MissingFieldsException("Game cannot be null.");
        }
        if (!gameRepository.existsById(game.getId())) {
            throw new ObjectNotFoundException("Game not found.");
        }
        return gameReviewRepository.findByGameOrderByDatePostedDesc(game);
    }

    @Transactional
    public List<GameReview> getReviewsByPlayer(Player reviewer) {
        if (reviewer == null) {
            throw new MissingFieldsException("Reviewer cannot be null.");
        }
        if (!playerRepository.existsById(reviewer.getId())) {
            throw new ObjectNotFoundException("Reviewer not found.");
        }
        return gameReviewRepository.findByReviewer(reviewer);
    }

    @Transactional(readOnly = true)
    public List<GameReview> getReviewsSortedByRating(Game game, boolean ascending) {
        if (game == null) {
            throw new MissingFieldsException("Game cannot be null.");
        }
        if (!gameRepository.existsById(game.getId())) {
            throw new ObjectNotFoundException("Game not found.");
        }
        if (ascending) {
            return gameReviewRepository.findByGameOrderByRatingAsc(game);
        } else {
            return gameReviewRepository.findByGameOrderByRatingDesc(game);
        }
    }
}