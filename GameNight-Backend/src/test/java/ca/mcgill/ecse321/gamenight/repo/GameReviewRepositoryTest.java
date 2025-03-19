package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.*;

@SpringBootTest
public class GameReviewRepositoryTest {

    @Autowired
    private GameReviewRepository gameReviewRepo;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private PlayerRepository playerRepo;

    private Person person;
    private Player player;
    private Game game;

    @BeforeEach
    public void setUp() {
        clearDatabase();

        person = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person);

        player = new Player(person);
        playerRepo.save(player);

        game = new Game("Batman", "A Batman game");
        gameRepo.save(game);
    }

    @AfterEach
    public void clearDatabase() {
        gameReviewRepo.deleteAll();
        gameRepo.deleteAll();
        playerRepo.deleteAll();
        personRepo.deleteAll();
    }

    @Test
    public void testCreateAndReadGameReview() {
        GameReview gameReview = new GameReview(5, "Great game!", player, game);
        gameReviewRepo.save(gameReview);

        GameReview retrievedGameReview = gameReviewRepo.findById(gameReview.getId()).orElse(null);

        assertNotNull(retrievedGameReview);
        assertEquals(gameReview.getRating(), retrievedGameReview.getRating());
        assertEquals(gameReview.getComment(), retrievedGameReview.getComment());
        assertEquals(gameReview.getGame().getId(), retrievedGameReview.getGame().getId());
        assertEquals(gameReview.getReviewer().getPerson().getName(),
                retrievedGameReview.getReviewer().getPerson().getName());
    }

    @Test
    public void testDeleteGameReview() {
        GameReview gameReview = new GameReview(5, "Great game!", player, game);
        gameReviewRepo.save(gameReview);

        gameReviewRepo.delete(gameReview);

        GameReview deletedGameReview = gameReviewRepo.findById(gameReview.getId()).orElse(null);

        assertNull(deletedGameReview);
    }

    @Test
    public void testFindByGame() {

        GameReview gameReview1 = new GameReview(5, "Great game!", player, game);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        GameReview gameReview2 = new GameReview(4, "Good game!", player, game);

        gameReviewRepo.save(gameReview1);
        gameReviewRepo.save(gameReview2);

        List<GameReview> reviews = gameReviewRepo.findByGame(game);

        assertEquals(2, reviews.size());

        assertTrue(reviews.stream().anyMatch(review -> matchesGameReview(review, gameReview1)),
                "GameReview1 not found in the list");
        assertTrue(reviews.stream().anyMatch(review -> matchesGameReview(review, gameReview2)),
                "GameReview2 not found in the list");
    }

    @Test
    public void testFindByGameOrderByDatePostedDesc() {

        GameReview gameReview1 = new GameReview(5, "Great game!", player, game);
        gameReviewRepo.save(gameReview1);

        GameReview gameReview2 = new GameReview(4, "Good game!", player, game);
        gameReviewRepo.save(gameReview2);

        List<GameReview> reviews = gameReviewRepo.findByGameOrderByDatePostedDesc(game);

        assertEquals(2, reviews.size(), "There should be exactly 2 reviews");
        assertTrue(matchesGameReview(reviews.get(0), gameReview2), "First review should be the most recent one");
        assertTrue(matchesGameReview(reviews.get(1), gameReview1), "Second review should be the older one");
    }

    @Test
    public void testFindByReviewer() {

        GameReview gameReview1 = new GameReview(5, "Great game!", player, game);
        GameReview gameReview2 = new GameReview(4, "Good game!", player, game);
        gameReviewRepo.save(gameReview1);
        gameReviewRepo.save(gameReview2);

        List<GameReview> reviews = gameReviewRepo.findByReviewer(player);

        assertEquals(2, reviews.size());

        assertTrue(reviews.stream().anyMatch(review -> matchesGameReview(review, gameReview1)));
        assertTrue(reviews.stream().anyMatch(review -> matchesGameReview(review, gameReview2)));
    }

    @Test
    public void testFindByGameOrderByRatingAsc() {
        GameReview gameReview1 = new GameReview(5, "Great game!", player, game);
        GameReview gameReview2 = new GameReview(4, "Good game!", player, game);
        gameReviewRepo.save(gameReview1);
        gameReviewRepo.save(gameReview2);

        List<GameReview> reviews = gameReviewRepo.findByGameOrderByRatingAsc(game);

        assertEquals(2, reviews.size());
        assertEquals(gameReview2.getId(), reviews.get(0).getId());
        assertEquals(4, reviews.get(0).getRating());
        assertEquals(gameReview1.getId(), reviews.get(1).getId());
        assertEquals(5, reviews.get(1).getRating());
    }

    @Test
    public void testFindByGameOrderByRatingDesc() {
        GameReview gameReview1 = new GameReview(5, "Great game!", player, game);
        GameReview gameReview2 = new GameReview(4, "Good game!", player, game);
        gameReviewRepo.save(gameReview1);
        gameReviewRepo.save(gameReview2);

        List<GameReview> reviews = gameReviewRepo.findByGameOrderByRatingDesc(game);

        assertEquals(2, reviews.size());
        assertEquals(gameReview1.getId(), reviews.get(0).getId());
        assertEquals(5, reviews.get(0).getRating());
        assertEquals(gameReview2.getId(), reviews.get(1).getId());
        assertEquals(4, reviews.get(1).getRating());
    }

    @Test
    public void testUpdateGameReview() {
        GameReview gameReview = new GameReview(5, "Great game!", player, game);
        gameReviewRepo.save(gameReview);

        gameReview.setRating(3);
        gameReview.setComment("Decent game.");
        gameReviewRepo.save(gameReview);

        GameReview updatedGameReview = gameReviewRepo.findById(gameReview.getId()).orElse(null);

        assertNotNull(updatedGameReview);
        assertEquals(3, updatedGameReview.getRating());
        assertEquals("Decent game.", updatedGameReview.getComment());
    }

    // helper
    private boolean matchesGameReview(GameReview actual, GameReview expected) {
        return actual.getId() == expected.getId() &&
                actual.getRating() == expected.getRating() &&
                actual.getComment().equals(expected.getComment()) &&
                actual.getGame().getId() == expected.getGame().getId() &&
                actual.getReviewer().getId() == expected.getReviewer().getId();
    }

}