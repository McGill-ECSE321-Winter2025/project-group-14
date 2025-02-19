package ca.mcgill.ecse321.gamenight.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import ca.mcgill.ecse321.gamenight.model.ScheduledGame;


public interface ScheduledGameRepository extends CrudRepository<ScheduledGame, Integer> {
    ScheduledGame findByKey(ScheduledGame.Key key);

    List<ScheduledGame> findByKey_GameId(int gameId);
    List<ScheduledGame> findByKey_EventId(int eventId);

}