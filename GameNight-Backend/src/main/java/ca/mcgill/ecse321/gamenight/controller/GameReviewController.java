package ca.mcgill.ecse321.gamenight.controller;

import ca.mcgill.ecse321.gamenight.dto.GameReviewDto;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.service.GameReviewService;
import ca.mcgill.ecse321.gamenight.service.GameService;
import ca.mcgill.ecse321.gamenight.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/game-reviews")
public class GameReviewController {

    @Autowired
    private GameReviewService gameReviewService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GameService gameService;

    private GameReviewDto convertToDto(GameReview review) {
        return new GameReviewDto(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getReviewer().getPlayerId(),
                review.getGame().getGameId());
    }

    private GameReview convertToEntity(GameReviewDto reviewDto) {
        Player reviewer = playerService.getPlayerById(reviewDto.getReviewerId());
        Game game = gameService.getGameById(reviewDto.getGameId());
        return new GameReview(
                reviewDto.getRating(),
                reviewDto.getComment(),
                reviewer,
                game);
    }

    @PostMapping("/submit")
    public ResponseEntity<GameReviewDto> submitReview(@RequestBody GameReviewDto reviewDto) {
        try {
            GameReview review = convertToEntity(reviewDto);
            GameReview createdReview = gameReviewService.submitReview(
                    review.getRating(),
                    review.getComment(),
                    review.getReviewer(),
                    review.getGame());
            return new ResponseEntity<>(convertToDto(createdReview), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable int reviewId) {
        boolean deleted = gameReviewService.deleteReview(reviewId);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update/{reviewId}")
    public ResponseEntity<GameReviewDto> updateReview(
            @PathVariable int reviewId,
            @RequestBody GameReviewDto reviewDto) {
        try {
            GameReview review = convertToEntity(reviewDto);
            GameReview updatedReview = gameReviewService.updateReview(reviewId, review.getRating(),
                    review.getComment());
            return new ResponseEntity<>(convertToDto(updatedReview), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<GameReviewDto> getReviewById(@PathVariable int reviewId) {
        try {
            GameReview review = gameReviewService.getReviewById(reviewId);
            return new ResponseEntity<>(convertToDto(review), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/reviews-for-game/{gameId}")
    public ResponseEntity<List<GameReviewDto>> getReviewsForGame(@PathVariable int gameId) {
        try {
            Game game = gameService.getGameById(gameId);
            List<GameReview> reviews = gameReviewService.getReviewsForGame(game);
            List<GameReviewDto> reviewDtos = reviews.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(reviewDtos, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/reviews-by-user/{reviewerId}")
    public ResponseEntity<List<GameReviewDto>> getReviewsByUser(@PathVariable int reviewerId) {
        try {
            Player reviewer = playerService.getPlayerById(reviewerId);
            List<GameReview> reviews = gameReviewService.getReviewsByUser(reviewer);
            List<GameReviewDto> reviewDtos = reviews.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(reviewDtos, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}