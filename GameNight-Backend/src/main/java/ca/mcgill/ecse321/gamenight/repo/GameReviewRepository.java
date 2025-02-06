package ca.mcgill.ecse321.gamenight.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;

public interface GameReviewRepository extends CrudRepository<GameReview, Integer>{

    List<GameReview> findByGameOrderByDatePostedDesc(Game game);
}
