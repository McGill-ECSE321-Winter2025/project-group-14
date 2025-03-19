package ca.mcgill.ecse321.gamenight.controller;

import ca.mcgill.ecse321.gamenight.dto.GameReviewDto;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.service.GameReviewService;
import ca.mcgill.ecse321.gamenight.service.GameManagementService;
import ca.mcgill.ecse321.gamenight.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ca.mcgill.ecse321.gamenight.exception.MissingFieldsException;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.exception.InvalidInputException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GameReviewController {

    @Autowired
    private GameReviewService gameReviewService;

    @Autowired
    private UserManagementService userService;

    @Autowired
    private GameManagementService gameService;

    private GameReviewDto convertToDto(GameReview review) {
        return new GameReviewDto(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getReviewer().getId(),
                review.getGame().getId());
    }

    private GameReview convertToEntity(GameReviewDto reviewDto) {
        Player reviewer = userService.getPlayerById(reviewDto.getReviewerId());
        Game game = gameService.findGameById(reviewDto.getGameId());
        return new GameReview(
                reviewDto.getRating(),
                reviewDto.getComment(),
                reviewer,
                game);
    }

    @PostMapping("/reviews/")
    public ResponseEntity<GameReviewDto> submitReview(@RequestBody GameReviewDto reviewDto) {
        try {
            GameReview review = convertToEntity(reviewDto);
            GameReview createdReview = gameReviewService.submitReview(
                    review.getRating(),
                    review.getComment(),
                    review.getReviewer(),
                    review.getGame());
            return new ResponseEntity<>(convertToDto(createdReview), HttpStatus.CREATED);
        } catch (InvalidInputException | MissingFieldsException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (ObjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable int reviewId) {

        boolean deleted = gameReviewService.deleteReview(reviewId);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<GameReviewDto> updateReview(
            @PathVariable int reviewId,
            @RequestBody GameReviewDto reviewDto) {
        try {
            GameReview review = convertToEntity(reviewDto);
            GameReview updatedReview = gameReviewService.updateReview(reviewId, review.getRating(),
                    review.getComment());
            return new ResponseEntity<>(convertToDto(updatedReview), HttpStatus.OK);
        } catch (InvalidInputException | MissingFieldsException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (ObjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<GameReviewDto> getReviewById(@PathVariable int reviewId) {
        try {
            GameReview review = gameReviewService.getReviewById(reviewId);
            return new ResponseEntity<>(convertToDto(review), HttpStatus.OK);
        } catch (ObjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/games/{gameId}/reviews")
    public ResponseEntity<List<GameReviewDto>> getReviewsForGame(@PathVariable int gameId) {
        try {
            Game game = gameService.findGameById(gameId);
            List<GameReview> reviews = gameReviewService.getReviewsForGame(game);
            List<GameReviewDto> reviewDtos = reviews.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(reviewDtos, HttpStatus.OK);
        } catch (ObjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/{reviewerId}/reviews")
    public ResponseEntity<List<GameReviewDto>> getReviewsByUser(@PathVariable int reviewerId) {
        try {
            Player reviewer = userService.getPlayerById(reviewerId);
            List<GameReview> reviews = gameReviewService.getReviewsByPlayer(reviewer);
            List<GameReviewDto> reviewDtos = reviews.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(reviewDtos, HttpStatus.OK);
        } catch (ObjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/games/{gameId}/average-rating")
    public ResponseEntity<Double> getAverageRatingForGame(@PathVariable int gameId) {
        try {
            Game game = gameService.findGameById(gameId);
            double averageRating = gameReviewService.getAverageRatingForGame(game);
            return new ResponseEntity<>(averageRating, HttpStatus.OK);
        } catch (ObjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/games/{gameId}/reviews-sorted")
    public ResponseEntity<List<GameReviewDto>> getReviewsSortedByRating(
            @PathVariable int gameId,
            @RequestParam boolean ascending) {
        try {
            Game game = gameService.findGameById(gameId);
            List<GameReview> reviews = gameReviewService.getReviewsSortedByRating(game, ascending);
            List<GameReviewDto> reviewDtos = reviews.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(reviewDtos, HttpStatus.OK);
        } catch (ObjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}