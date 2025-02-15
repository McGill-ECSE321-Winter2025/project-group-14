package ca.mcgill.ecse321.gamenight.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.gamenight.model.Game;

public interface GameRepository extends CrudRepository<Game, Integer> {
    @Query("SELECT r.game FROM GameReview r GROUP BY r.game ORDER BY AVG(r.rating) DESC LIMIT 3")
    List<Game> findTheThreeHighestRatedGames();
}
