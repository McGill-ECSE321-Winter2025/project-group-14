package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gamenight.controller.GameReviewController;
import ca.mcgill.ecse321.gamenight.dto.GameReviewDto;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.repo.GameReviewRepository;

@SpringBootTest
@Transactional
public class GameReviewIntegrationTest {

    @Autowired
    private GameReviewController gameReviewController;

    @Autowired
    private GameReviewRepository gameReviewRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PersonRepository personRepository;

    private Person person;
    private Player reviewer;
    private Game game;

    @BeforeEach
    public void setup() {

        gameReviewRepository.deleteAll();
        gameRepository.deleteAll();
        playerRepository.deleteAll();
        personRepository.deleteAll();

        person = new Person("john@example.com", "password123", "John Doe");
        personRepository.save(person);

        reviewer = new Player(person);
        playerRepository.save(reviewer);

        game = new Game("Uno", "A card game");
        gameRepository.save(game);
    }

    @Test
    public void testSubmitValidReview() {

        GameReviewDto reviewDto = new GameReviewDto(0, 5, "Great game!", reviewer.getId(), game.getId());

        ResponseEntity<GameReviewDto> response = gameReviewController.submitReview(reviewDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getRating());
        assertEquals("Great game!", response.getBody().getComment());
    }

    @Test
    public void testSubmitReviewWithInvalidRating() {

        GameReviewDto reviewDto = new GameReviewDto(0, 0, "Great game!", reviewer.getId(), game.getId());

        ResponseEntity<GameReviewDto> response = gameReviewController.submitReview(reviewDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testSubmitReviewNonExistingGame() {
        int nonExistingGameId = 999;
        GameReviewDto reviewDto = new GameReviewDto(0, 5, "Great game!", reviewer.getId(), nonExistingGameId);

        ResponseEntity<GameReviewDto> response = gameReviewController.submitReview(reviewDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteExistingReview() {

        GameReview review = new GameReview(5, "Great game!", reviewer, game);
        gameReviewRepository.save(review);

        ResponseEntity<Void> response = gameReviewController.deleteReview(review.getId());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testDeleteNonExistingReview() {

        ResponseEntity<Void> response = gameReviewController.deleteReview(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testUpdateReview() {

        GameReview review = new GameReview(3, "Average game", reviewer, game);
        gameReviewRepository.save(review);

        GameReviewDto reviewDto = new GameReviewDto(review.getId(), 5, "Great game!", reviewer.getId(), game.getId());
        ResponseEntity<GameReviewDto> response = gameReviewController.updateReview(review.getId(), reviewDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getRating());
        assertEquals("Great game!", response.getBody().getComment());
    }

    @Test
    public void testUpdateReviewWithInvalidRating() {

        GameReview review = new GameReview(3, "Average game", reviewer, game);
        gameReviewRepository.save(review);

        GameReviewDto reviewDto = new GameReviewDto(review.getId(), 6, "Great game!", reviewer.getId(), game.getId());
        ResponseEntity<GameReviewDto> response = gameReviewController.updateReview(review.getId(), reviewDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testUpdateNonExistingReview() {

        int nonExistingReviewId = 999;
        GameReviewDto reviewDto = new GameReviewDto(nonExistingReviewId, 5, "Great game!", reviewer.getId(),
                game.getId());

        ResponseEntity<GameReviewDto> response = gameReviewController.updateReview(nonExistingReviewId, reviewDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetReviewById() {

        GameReview review = new GameReview(5, "Great game!", reviewer, game);
        gameReviewRepository.save(review);

        ResponseEntity<GameReviewDto> response = gameReviewController.getReviewById(review.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getRating());
        assertEquals("Great game!", response.getBody().getComment());
    }

    @Test
    public void testGetReviewByIdNotFound() {

        ResponseEntity<GameReviewDto> response = gameReviewController.getReviewById(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetReviewsForGame() {

        GameReview review = new GameReview(5, "Great game!", reviewer, game);
        gameReviewRepository.save(review);

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsForGame(game.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetReviewsForNonExistingGame() {

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsForGame(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetReviewsByUser() {

        GameReview review = new GameReview(5, "Great game!", reviewer, game);
        gameReviewRepository.save(review);

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsByUser(reviewer.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetReviewsForNonExistingUser() {

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsByUser(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetAverageRatingForGame() {

        GameReview review1 = new GameReview(5, "Great game!", reviewer, game);
        GameReview review2 = new GameReview(4, "Good game!", reviewer, game);
        gameReviewRepository.save(review1);
        gameReviewRepository.save(review2);

        ResponseEntity<Double> response = gameReviewController.getAverageRatingForGame(game.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4.5, response.getBody());
    }

    @Test
    public void testGetAverageRatingForInvalidGame() {

        ResponseEntity<Double> response = gameReviewController.getAverageRatingForGame(999);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetReviewsSortedByRating() {

        GameReview review1 = new GameReview(3, "Average game", reviewer, game);
        GameReview review2 = new GameReview(5, "Great game!", reviewer, game);
        gameReviewRepository.save(review1);
        gameReviewRepository.save(review2);

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsSortedByRating(game.getId(),
                true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(3, response.getBody().get(0).getRating());
        assertEquals(5, response.getBody().get(1).getRating());
    }

    @Test
    public void testGetReviewsSortedByRatingInvalid() {

        ResponseEntity<List<GameReviewDto>> response = gameReviewController.getReviewsSortedByRating(999, true);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}