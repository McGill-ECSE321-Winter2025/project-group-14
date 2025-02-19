package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.ScheduledGame;
import ca.mcgill.ecse321.gamenight.model.ScheduledGame.Key;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.Event;

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

        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 7200000); // 2 hours later
        Event event = new Event("Game Night", "An evening of board games", startTime, endTime);
        event = eventRepo.save(event);

        ScheduledGame.Key key = new ScheduledGame.Key(game, event);
        ScheduledGame scheduledGame = new ScheduledGame(key);
        scheduledGame = scheduledGameRepo.save(scheduledGame);

        ScheduledGame fetchedScheduledGame = scheduledGameRepo.findByKey(key);

        assertNotNull(fetchedScheduledGame, "Scheduled game should be present in repository");
        assertEquals(game.getName(), fetchedScheduledGame.getKey().getGame().getName());
        assertEquals(event.getName(), fetchedScheduledGame.getKey().getEvent().getName());
        assertEquals(startTime, fetchedScheduledGame.getKey().getEvent().getStartTime());
        assertEquals(endTime, fetchedScheduledGame.getKey().getEvent().getEndTime());
    }

    @Test
    public void testFindByGame() {
        Game game = new Game("Catan", "Board game");
        game = gameRepo.save(game);

        Date startTime = new Date();
        Event event1 = new Event("Game Night One", "Board games night", startTime, new Date(startTime.getTime() + 3600000));
        Event event2 = new Event("Game Night Two", "More board games", startTime, new Date(startTime.getTime() + 3600000));
        event1 = eventRepo.save(event1);
        event2 = eventRepo.save(event2);

        ScheduledGame sg1 = new ScheduledGame(new Key(game, event1));
        ScheduledGame sg2 = new ScheduledGame(new Key(game, event2));
        scheduledGameRepo.save(sg1);
        scheduledGameRepo.save(sg2);

        List<ScheduledGame> gamesList = scheduledGameRepo.findByKey_GameId(game.getId());
        assertNotNull(gamesList, "Games list should not be null");
        assertEquals(2, gamesList.size(), "There should be two scheduled games for the game");
    }

    @Test
    public void testFindByEvent() {
        Game game1 = new Game("Catan", "Strategy game");
        Game game2 = new Game("Monopoly", "Economic game");
        game1 = gameRepo.save(game1);
        game2 = gameRepo.save(game2);

        Date startTime = new Date();
        Event event = new Event("Board Game Marathon", "A day of games", startTime, new Date(startTime.getTime() + 7200000));
        event = eventRepo.save(event);

        ScheduledGame sg1 = new ScheduledGame(new Key(game1, event));
        ScheduledGame sg2 = new ScheduledGame(new Key(game2, event));
        scheduledGameRepo.save(sg1);
        scheduledGameRepo.save(sg2);

        List<ScheduledGame> eventsList = scheduledGameRepo.findByKey_EventId(event.getId());
        assertNotNull(eventsList, "Events list should not be null");
        assertEquals(2, eventsList.size(), "There should be two scheduled games for the event");
    }
}
