package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;

@SpringBootTest
public class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepo;
    @Autowired
    private GameReviewRepository reviewRepo;

    @AfterEach
	public void clearDatabase() {
        reviewRepo.deleteAll();
		gameRepo.deleteAll();
	}

    @Test
    public void testCreateGame() {
        Game game = new Game("Uno", "A card game");
        game = gameRepo.save(game);

        Optional<Game> fetchedGame = gameRepo.findById(game.getName());

        assertNotNull(game);
        assertEquals(game.getName(), fetchedGame.get().getName());
        assertEquals(game.getDescription(), fetchedGame.get().getDescription());
    }

    @Test
    public void findTheThreeHighestRatedGamesTest() {
        Game game1 = new Game("Uno", null);
        gameRepo.save(game1);
        Game game2 = new Game("Monopoly", null);
        gameRepo.save(game2);
        Game game3 = new Game("Twister", null);
        gameRepo.save(game3);
        Game game4 = new Game("Chess", null);
        gameRepo.save(game4);
        GameReview r1 = new GameReview(7, null, null, game1);
        reviewRepo.save(r1);
        GameReview r2 = new GameReview(7, null, null, game2);
        reviewRepo.save(r2);
        GameReview r3 = new GameReview(8, null, null, game2);
        reviewRepo.save(r3);
        GameReview r4 = new GameReview(4, null, null, game3);
        reviewRepo.save(r4);
        GameReview r5 = new GameReview(6, null, null, game4);
        reviewRepo.save(r5);

        List<Game> topGames = gameRepo.findTheThreeHighestRatedGames();

        assertNotNull(topGames);
        assertEquals(3, topGames.size());
        Game firstGame = topGames.get(0);
        assertEquals("Monopoly", firstGame.getName());
        Game secondGame = topGames.get(1);
        assertEquals("Uno", secondGame.getName());
        Game thirdGame = topGames.get(2);
        assertEquals("Chess", thirdGame.getName());
    }
}
