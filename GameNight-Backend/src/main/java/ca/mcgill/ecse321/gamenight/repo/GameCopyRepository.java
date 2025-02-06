package ca.mcgill.ecse321.gamenight.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;

public interface GameCopyRepository extends CrudRepository<GameCopy, Integer>{

    List<GameCopy> findByGame(Game game);
    
    List<GameCopy> findByGameOwner(GameOwner gameOwner);
}
