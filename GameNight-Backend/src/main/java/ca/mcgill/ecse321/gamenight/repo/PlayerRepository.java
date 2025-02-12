package ca.mcgill.ecse321.gamenight.repo;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Player;

public interface PlayerRepository extends CrudRepository<Player, Integer> {
    
    Player findPlayerByEvent(Event event);

    Player findPlayerByGameReview(GameReview review );
}
