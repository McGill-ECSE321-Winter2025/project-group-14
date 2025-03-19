package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.exception.MissingFieldsException;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.exception.InvalidInputException;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.GameReviewRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;

@SpringBootTest
public class GameReviewServiceTest {

    @Mock
    private GameReviewRepository gameReviewRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private GameReviewService gameReviewService;

    private Person person;
    private Player reviewer;
    private Game game;

    @BeforeEach
    public void setup() {
        person = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        reviewer = new Player(person);
        game = new Game("Uno", "A card game");

        when(gameRepository.existsById(game.getId())).thenReturn(true);
        when(playerRepository.existsById(reviewer.getId())).thenReturn(true);
    }

    @Test
    public void testSubmitValidReview() {
        GameReview review = new GameReview(5, "Great game!", reviewer, game);
        when(gameReviewRepository.save(any(GameReview.class))).thenReturn(review);

        GameReview submittedReview = gameReviewService.submitReview(5, "Great game!", reviewer, game);

        assertNotNull(submittedReview);
        assertEquals(5, submittedReview.getRating());
        assertEquals("Great game!", submittedReview.getComment());
        assertEquals(reviewer, submittedReview.getReviewer());
        assertEquals(game, submittedReview.getGame());
    }

    @Test
    public void testSubmitReviewWithTooLowRating() {
        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> gameReviewService.submitReview(0, "Great game!", reviewer, game));

        assertEquals("Rating must be between 1 and 5.", e.getMessage());
    }

    @Test
    public void testSubmitReviewWithTooHighRating() {
        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> gameReviewService.submitReview(6, "Great game!", reviewer, game));

        assertEquals("Rating must be between 1 and 5.", e.getMessage());
    }

    @Test
    public void testSubmitReviewWithEmptyComment() {
        MissingFieldsException e = assertThrows(MissingFieldsException.class,
                () -> gameReviewService.submitReview(5, "", reviewer, game));

        assertEquals("Comment cannot be empty.", e.getMessage());
    }

    @Test
    public void testSubmitReviewWithNullComment() {
        MissingFieldsException e = assertThrows(MissingFieldsException.class,
                () -> gameReviewService.submitReview(5, null, reviewer, game));

        assertEquals("Comment cannot be empty.", e.getMessage());
    }

    @Test
    public void testSubmitReviewWithNullReviewer() {
        MissingFieldsException e = assertThrows(MissingFieldsException.class,
                () -> gameReviewService.submitReview(5, "Great game!", null, game));

        assertEquals("Reviewer cannot be null.", e.getMessage());
    }

    @Test
    public void testSubmitReviewWithNullGame() {
        MissingFieldsException e = assertThrows(MissingFieldsException.class,
                () -> gameReviewService.submitReview(5, "Great game!", reviewer, null));

        assertEquals("Game cannot be null.", e.getMessage());
    }

    @Test
    public void testDeleteExistingReview() {
        int reviewId = 1;
        when(gameReviewRepository.existsById(reviewId)).thenReturn(true);

        boolean result = gameReviewService.deleteReview(reviewId);

        assertTrue(result);
        verify(gameReviewRepository, times(1)).deleteById(reviewId);
    }

    @Test
    public void testDeleteNonExistingReview() {
        int reviewId = 1;
        when(gameReviewRepository.existsById(reviewId)).thenReturn(false);

        boolean result = gameReviewService.deleteReview(reviewId);

        assertFalse(result);
        verify(gameReviewRepository, never()).deleteById(reviewId);
    }

    @Test
    public void testUpdateReview() {
        int reviewId = 1;
        GameReview review = new GameReview(3, "Average game", reviewer, game);
        when(gameReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(gameReviewRepository.save(any(GameReview.class))).thenReturn(review);

        GameReview updatedReview = gameReviewService.updateReview(reviewId, 4, "Improved game");

        assertNotNull(updatedReview);
        assertEquals(4, updatedReview.getRating());
        assertEquals("Improved game", updatedReview.getComment());
    }

    @Test
    public void testUpdateReviewWithTooHighRating() {
        int reviewId = 1;
        GameReview review = new GameReview(3, "Average game", reviewer, game);
        when(gameReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> gameReviewService.updateReview(reviewId, 6, "Improved game"));

        assertEquals("Rating must be between 1 and 5.", e.getMessage());
    }

    @Test
    public void testUpdateReviewWithTooLowRating() {
        int reviewId = 1;
        GameReview review = new GameReview(3, "Average game", reviewer, game);
        when(gameReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> gameReviewService.updateReview(reviewId, 0, "Worst game"));

        assertEquals("Rating must be between 1 and 5.", e.getMessage());
    }

    @Test
    public void testUpdateReviewWithEmptyComment() {
        int reviewId = 1;
        GameReview review = new GameReview(3, "Average game", reviewer, game);
        when(gameReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        MissingFieldsException e = assertThrows(MissingFieldsException.class,
                () -> gameReviewService.updateReview(reviewId, 4, ""));

        assertEquals("Comment cannot be empty.", e.getMessage());
    }

    @Test
    public void testUpdateReviewWithNullComment() {
        int reviewId = 1;
        GameReview review = new GameReview(3, "Average game", reviewer, game);
        when(gameReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        MissingFieldsException e = assertThrows(MissingFieldsException.class,
                () -> gameReviewService.updateReview(reviewId, 4, null));

        assertEquals("Comment cannot be empty.", e.getMessage());
    }

    @Test
    public void testUpdateReviewNotFound() {
        int nonExistingReviewId = 999;
        when(gameReviewRepository.findById(nonExistingReviewId)).thenReturn(Optional.empty());

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class,
                () -> gameReviewService.updateReview(nonExistingReviewId, 4, "Updated comment"));

        assertEquals("Review not found.", e.getMessage());
    }

    @Test
    public void testGetReviewById() {
        int reviewId = 0;
        GameReview review = new GameReview(5, "Great game!", reviewer, game);
        when(gameReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        GameReview foundReview = gameReviewService.getReviewById(reviewId);

        assertNotNull(foundReview);
        assertEquals(reviewId, foundReview.getId());
    }

    @Test
    public void testGetReviewByIdNotFound() {
        int reviewId = 0;
        when(gameReviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class,
                () -> gameReviewService.getReviewById(reviewId));

        assertEquals("Review not found.", e.getMessage());
    }

    @Test
    public void testGetAverageRatingForGame() {
        List<GameReview> reviews = new ArrayList<>();
        reviews.add(new GameReview(5, "Great game!", reviewer, game));
        reviews.add(new GameReview(4, "Good game!", reviewer, game));
        when(gameReviewRepository.findByGame(game)).thenReturn(reviews);

        double averageRating = gameReviewService.getAverageRatingForGame(game);

        assertEquals(4.5, averageRating);
    }

    @Test
    public void testGetAverageRatingForGameWithNoReviews() {
        when(gameReviewRepository.findByGame(game)).thenReturn(new ArrayList<>());

        double averageRating = gameReviewService.getAverageRatingForGame(game);

        assertEquals(0.0, averageRating);
    }

    @Test
    public void testGetAverageRatingForNullGame() {
        MissingFieldsException e = assertThrows(MissingFieldsException.class,
                () -> gameReviewService.getAverageRatingForGame(null));

        assertEquals("Game cannot be null.", e.getMessage());
    }

    @Test
    public void testGetReviewsForGame() {
        List<GameReview> reviews = new ArrayList<>();
        reviews.add(new GameReview(5, "Great game!", reviewer, game));
        reviews.add(new GameReview(4, "Good game!", reviewer, game));
        when(gameReviewRepository.findByGameOrderByDatePostedDesc(game)).thenReturn(reviews);

        List<GameReview> foundReviews = gameReviewService.getReviewsForGame(game);

        assertEquals(2, foundReviews.size());
    }

    @Test
    public void testGetReviewsForNullGame() {
        MissingFieldsException e = assertThrows(MissingFieldsException.class,
                () -> gameReviewService.getReviewsForGame(null));

        assertEquals("Game cannot be null.", e.getMessage());
    }

    @Test
    public void testGetReviewsByPlayer() {
        List<GameReview> reviews = new ArrayList<>();
        reviews.add(new GameReview(5, "Great game!", reviewer, game));
        reviews.add(new GameReview(4, "Good game!", reviewer, game));
        when(gameReviewRepository.findByReviewer(reviewer)).thenReturn(reviews);

        List<GameReview> foundReviews = gameReviewService.getReviewsByPlayer(reviewer);

        assertEquals(2, foundReviews.size());
    }

    @Test
    public void testGetReviewsForNullPlayer() {
        MissingFieldsException e = assertThrows(MissingFieldsException.class,
                () -> gameReviewService.getReviewsByPlayer(null));

        assertEquals("Reviewer cannot be null.", e.getMessage());
    }

    @Test
    public void testGetReviewsSortedByRatingAscending() {
        List<GameReview> reviews = new ArrayList<>();
        reviews.add(new GameReview(3, "Average game", reviewer, game));
        reviews.add(new GameReview(5, "Great game!", reviewer, game));
        when(gameReviewRepository.findByGameOrderByRatingAsc(game)).thenReturn(reviews);

        List<GameReview> sortedReviews = gameReviewService.getReviewsSortedByRating(game, true);

        assertEquals(3, sortedReviews.get(0).getRating());
        assertEquals(5, sortedReviews.get(1).getRating());
    }

    @Test
    public void testGetReviewsSortedByRatingDescending() {
        List<GameReview> reviews = new ArrayList<>();
        reviews.add(new GameReview(5, "Great game!", reviewer, game));
        reviews.add(new GameReview(3, "Average game", reviewer, game));
        when(gameReviewRepository.findByGameOrderByRatingDesc(game)).thenReturn(reviews);

        List<GameReview> sortedReviews = gameReviewService.getReviewsSortedByRating(game, false);

        assertEquals(5, sortedReviews.get(0).getRating());
        assertEquals(3, sortedReviews.get(1).getRating());
    }

    @Test
    public void testGetReviewsSortedForNullGame() {
        MissingFieldsException e = assertThrows(MissingFieldsException.class,
                () -> gameReviewService.getReviewsSortedByRating(null, true));

        assertEquals("Game cannot be null.", e.getMessage());
    }
}