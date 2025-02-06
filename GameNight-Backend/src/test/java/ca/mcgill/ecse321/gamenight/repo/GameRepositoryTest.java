package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.Game;
import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class GameRepositoryTest {

    @Autowired
    private GameRepository repo;

    @AfterEach
	public void clearDatabase() {
		repo.deleteAll();
	}

    @Test
    public void testCreateGame() {
        Game game = new Game("Uno", "A card game");
        game = repo.save(game);
        Optional<Game> fetchedGame = repo.findById(game.getName());
        assertNotNull(game);
        assertEquals(game.getName(), fetchedGame.get().getName());
        assertEquals(game.getDescription(), fetchedGame.get().getDescription());
    }
}
