package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.*;

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
}