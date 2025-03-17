package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gamenight.controller.GameReviewController;
import ca.mcgill.ecse321.gamenight.dto.GameReviewDto;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.service.GameReviewService;
import ca.mcgill.ecse321.gamenight.service.GameManagementService;
import ca.mcgill.ecse321.gamenight.service.UserManagementService;

@SpringBootTest
public class GameReviewIntegrationTest {

    @Mock
    private GameReviewService gameReviewService;

    @Mock
    private UserManagementService userService;

    @Mock
    private GameManagementService gameService;

    @InjectMocks
    private GameReviewController gameReviewController;

    private Person person;
    private Player reviewer;
    private Game game;
    private GameReview review;

    @BeforeEach
    public void setup() {
        person = new Person("john@example.com", "password123", "John Doe");
        reviewer = new Player(person);
        game = new Game("Uno", "A card game");
        review = new GameReview(5, "Great game!", reviewer, game);
    }

    @Test
    public void testSubmitValidReview() {

        when(userService.getPlayerById(anyInt())).thenReturn(reviewer);
        when(gameService.findGameById(anyInt())).thenReturn(game);
        when(gameReviewService.submitReview(anyInt(), anyString(), any(Player.class), any(Game.class)))
                .thenReturn(review);

        GameReviewDto reviewDto = new GameReviewDto(0, 5, "Great game!", 1, 1);

        ResponseEntity<GameReviewDto> response = gameReviewController.submitReview(reviewDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getRating());
        assertEquals("Great game!", response.getBody().getComment());
    }

    @Test
    public void testSubmitReviewWithInvalidRating() {

        when(userService.getPlayerById(anyInt())).thenReturn(reviewer);
        when(gameService.findGameById(anyInt())).thenReturn(game);
        when(gameReviewService.submitReview(anyInt(), anyString(), any(Player.class), any(Game.class)))
                .thenThrow(new IllegalArgumentException("Rating must be between 1 and 5."));

        GameReviewDto reviewDto = new GameReviewDto(0, 0, "Great game!", 1, 1);

        ResponseEntity<GameReviewDto> response = gameReviewController.submitReview(reviewDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testDeleteExistingReview() {

        when(gameReviewService.deleteReview(anyInt())).thenReturn(true);

        ResponseEntity<Void> response = gameReviewController.deleteReview(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testDeleteNonExistingReview() {

        when(gameReviewService.deleteReview(anyInt())).thenReturn(false);

        ResponseEntity<Void> response = gameReviewController.deleteReview(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testUpdateReview() {

        when(gameReviewService.updateReview(anyInt(), anyInt(), anyString())).thenReturn(review);

        GameReviewDto reviewDto = new GameReviewDto(1, 5, "Great game!", 1, 1);

        ResponseEntity<GameReviewDto> response = gameReviewController.updateReview(1, reviewDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getRating());
        assertEquals("Great game!", response.getBody().getComment());
    }

    @Test
    public void testUpdateReviewWithInvalidRating() {

        when(gameReviewService.updateReview(anyInt(), anyInt(), anyString()))
                .thenThrow(new IllegalArgumentException("Rating must be between 1 and 5."));

        GameReviewDto reviewDto = new GameReviewDto(1, 6, "Great game!", 1, 1);

        ResponseEntity<GameReviewDto> response = gameReviewController.updateReview(1, reviewDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetReviewById() {

        when(gameReviewService.getReviewById(anyInt())).thenReturn(review);

        ResponseEntity<GameReviewDto> response = gameReviewController.getReviewById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getRating());
        assertEquals("Great game!", response.getBody().getComment());
    }

    @Test
    public void testGetReviewByIdNotFound() {

        when(gameReviewService.getReviewById(anyInt()))
                .thenThrow(new IllegalArgumentException("Review not found."));

        ResponseEntity<GameReviewDto> response = gameReviewController.getReviewById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetReviewsForGame() {

        List<GameReview> reviews = new ArrayList<>();
        reviews.add(review);
        when(gameService.findGameById(anyInt())).thenReturn(game);
        when(gameReviewService.getReviewsForGame(any(Game.class))).thenReturn(reviews);

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsForGame(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetReviewsForInvalidGame() {
        when(gameService.findGameById(anyInt())).thenThrow(new IllegalArgumentException("Game not found."));

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsForGame(999);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetReviewsByUser() {

        List<GameReview> reviews = new ArrayList<>();
        reviews.add(review);
        when(userService.getPlayerById(anyInt())).thenReturn(reviewer);
        when(gameReviewService.getReviewsByPlayer(any(Player.class))).thenReturn(reviews);

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsByUser(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetReviewsForInvalidUser() {
        when(userService.getPlayerById(anyInt())).thenThrow(new IllegalArgumentException("Player not found."));

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsByUser(999);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetAverageRatingForGame() {

        when(gameService.findGameById(anyInt())).thenReturn(game);
        when(gameReviewService.getAverageRatingForGame(any(Game.class))).thenReturn(4.5);

        ResponseEntity<Double> response = gameReviewController.getAverageRatingForGame(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4.5, response.getBody());
    }

    @Test
    public void testGetAverageRatingForInvalidGame() {
        when(gameService.findGameById(anyInt())).thenThrow(new IllegalArgumentException("Game not found."));

        ResponseEntity<Double> response = gameReviewController.getAverageRatingForGame(999);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetReviewsSortedByRating() {

        List<GameReview> reviews = new ArrayList<>();
        reviews.add(review);
        when(gameService.findGameById(anyInt())).thenReturn(game);
        when(gameReviewService.getReviewsSortedByRating(any(Game.class), anyBoolean())).thenReturn(reviews);

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsSortedByRating(1, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetReviewsSortedByRatingInvalid() {
        when(gameService.findGameById(anyInt())).thenThrow(new IllegalArgumentException("Game not found."));

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsSortedByRating(999, true);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}