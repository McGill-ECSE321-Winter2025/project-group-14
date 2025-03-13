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
        GameReview gameReview2 = new GameReview(4, "Good game!", player, game);
        gameReviewRepo.save(gameReview1);
        gameReviewRepo.save(gameReview2);

        List<GameReview> reviews = gameReviewRepo.findByGame(game);

        assertEquals(2, reviews.size());
        assertTrue(reviews.contains(gameReview1));
        assertTrue(reviews.contains(gameReview2));
    }

    @Test
    public void testFindByGameOrderByDatePostedDesc() {
        GameReview gameReview1 = new GameReview(5, "Great game!", player, game);
        GameReview gameReview2 = new GameReview(4, "Good game!", player, game);
        gameReviewRepo.save(gameReview1);
        gameReviewRepo.save(gameReview2);

        List<GameReview> reviews = gameReviewRepo.findByGameOrderByDatePostedDesc(game);

        assertEquals(2, reviews.size());
        assertEquals(gameReview2.getId(), reviews.get(0).getId());
        assertEquals(gameReview1.getId(), reviews.get(1).getId());
    }

    @Test
    public void testFindByReviewer() {
        GameReview gameReview1 = new GameReview(5, "Great game!", player, game);
        GameReview gameReview2 = new GameReview(4, "Good game!", player, game);
        gameReviewRepo.save(gameReview1);
        gameReviewRepo.save(gameReview2);

        List<GameReview> reviews = gameReviewRepo.findByReviewer(player);

        assertEquals(2, reviews.size());
        assertTrue(reviews.contains(gameReview1));
        assertTrue(reviews.contains(gameReview2));
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
        assertEquals(gameReview1.getId(), reviews.get(1).getId());
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
        assertEquals(gameReview2.getId(), reviews.get(1).getId());
    }
}