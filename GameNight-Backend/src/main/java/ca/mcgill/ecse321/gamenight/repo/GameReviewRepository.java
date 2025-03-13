package ca.mcgill.ecse321.gamenight.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Player;

public interface GameReviewRepository extends CrudRepository<GameReview, Integer> {
    List<GameReview> findByGame(Game game);

    List<GameReview> findByGameOrderByDatePostedDesc(Game game);

    List<GameReview> findByReviewer(Player reviewer);

    List<GameReview> findByGameOrderByRatingAsc(Game game);

    List<GameReview> findByGameOrderByRatingDesc(Game game);

}
