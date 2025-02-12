package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.ScheduledGame;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.repository.ScheduledGameRepository;
import ca.mcgill.ecse321.gamenight.repository.GameRepository;
import ca.mcgill.ecse321.gamenight.repository.EventRepository;

@SpringBootTest
public class ScheduledGameRepositoryTest {

    @Autowired
    private ScheduledGameRepository scheduledGameRepo;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private EventRepository eventRepo;

    @AfterEach
    public void clearDatabase() {
        scheduledGameRepo.deleteAll();
        eventRepo.deleteAll();
        gameRepo.deleteAll();
    }

    @Test
    public void testCreateScheduledGame() {
        Game game = new Game("Catan", "A strategy board game");
        game = gameRepo.save(game);

        Event event = new Event("Game Night", "An evening of board games", null, null);
        event = eventRepo.save(event);

        ScheduledGame scheduledGame = new ScheduledGame(game, event);
        scheduledGame = scheduledGameRepo.save(scheduledGame);

        Optional<ScheduledGame> fetchedScheduledGame = scheduledGameRepo.findById(scheduledGame.getId());

        assertNotNull(fetchedScheduledGame.get());
        assertEquals(game.getName(), fetchedScheduledGame.get().getGame().getName());
        assertEquals(event.getName(), fetchedScheduledGame.get().getEvent().getName());
    }
}
