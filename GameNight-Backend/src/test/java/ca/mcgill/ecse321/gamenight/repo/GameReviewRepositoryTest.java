package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;

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

    @AfterEach
    public void clearDatabase() {
        gameReviewRepo.deleteAll();
        gameRepo.deleteAll();
        playerRepo.deleteAll();
        personRepo.deleteAll();
    }

    @Test
    public void testCreateAndReadGameReview() {
        // Create a Person
        Person person = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person);

        // Create Player
        Player player = new Player();
        player.setPerson(person);
        playerRepo.save(player);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameReview
        GameReview gameReview = new GameReview(5, "Great game!", player, game);
        gameReviewRepo.save(gameReview);

        // Read the GameReview from the database
        GameReview retrievedGameReview = gameReviewRepo.findById(gameReview.getId()).orElse(null);

        // Assertions
        assertNotNull(retrievedGameReview);
        assertEquals(gameReview.getRating(), retrievedGameReview.getRating());
        assertEquals(gameReview.getComment(), retrievedGameReview.getComment());
        assertEquals(gameReview.getGame().getId(), retrievedGameReview.getGame().getId());
        assertEquals(gameReview.getReviewer().getPerson().getName(),
                retrievedGameReview.getReviewer().getPerson().getName());
    }

    @Test
    public void testDeleteGameReview() {
        // Create a Person
        Person person = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person);

        // Create Player
        Player player = new Player();
        player.setPerson(person);
        playerRepo.save(player);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameReview
        GameReview gameReview = new GameReview(5, "Great game!", player, game);
        gameReviewRepo.save(gameReview);

        // Delete the GameReview
        gameReviewRepo.delete(gameReview);

        // Try to retrieve the deleted GameReview
        GameReview deletedGameReview = gameReviewRepo.findById(gameReview.getId()).orElse(null);

        // Assertions
        assertNull(deletedGameReview);
    }
}