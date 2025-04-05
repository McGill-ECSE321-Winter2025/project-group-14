package ca.mcgill.ecse321.gamenight.repo;

import ca.mcgill.ecse321.gamenight.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class GameReviewRepositoryTest {

    @Autowired
    private GameReviewRepository gameReviewRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PersonRepository personRepository;

    private Game game;
    private Player player;

    @BeforeEach
    public void setUp() {
        gameReviewRepository.deleteAll();
        playerRepository.deleteAll();
        personRepository.deleteAll();
        gameRepository.deleteAll();

        // Create and save a unique person
        String uniqueEmail = "user_" + UUID.randomUUID().toString() + "@test.com";
        Person person = new Person(uniqueEmail, "password123", "Test User");
        personRepository.save(person);

        // Create and save a player linked to the person
        player = new Player();
        player.setPerson(person);
        playerRepository.save(player);

        // Create and save a game
        game = new Game("Chess", "A strategic board game");
        gameRepository.save(game);
    }

    @Test
    public void testFindByGameOrderByRatingDesc() {
        // Create reviews
        GameReview review1 = new GameReview(3, "Okay game", player, game);
        GameReview review2 = new GameReview(5, "Great game!", player, game);
        gameReviewRepository.save(review1);
        gameReviewRepository.save(review2);

        List<GameReview> reviews = gameReviewRepository.findByGameOrderByRatingDesc(game);
        assertEquals(2, reviews.size());
        assertEquals(5, reviews.get(0).getRating());
        assertEquals(3, reviews.get(1).getRating());
    }

    @Test
    public void testFindByGameOrderByDatePostedDesc() {
        // Create reviews
        GameReview review1 = new GameReview(4, "Nice game", player, game);
        GameReview review2 = new GameReview(2, "Not bad", player, game);
        gameReviewRepository.save(review1);
        gameReviewRepository.save(review2);

        List<GameReview> reviews = gameReviewRepository.findByGameOrderByDatePostedDesc(game);
        assertEquals(2, reviews.size());

        assertTrue(reviews.get(0).getDatePosted().after(reviews.get(1).getDatePosted())
                || reviews.get(0).getDatePosted().equals(reviews.get(1).getDatePosted()));
    }
}
