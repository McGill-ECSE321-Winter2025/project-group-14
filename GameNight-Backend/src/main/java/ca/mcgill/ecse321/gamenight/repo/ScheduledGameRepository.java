package ca.mcgill.ecse321.gamenight.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import ca.mcgill.ecse321.gamenight.model.ScheduledGame;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.Event;

public interface ScheduledGameRepository extends CrudRepository<ScheduledGame, Integer> {

    List<ScheduledGame> findByGame(Game game);

    List<ScheduledGame> findByEvent(Event event);
}
