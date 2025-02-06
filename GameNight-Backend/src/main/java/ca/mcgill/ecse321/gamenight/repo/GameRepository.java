package ca.mcgill.ecse321.gamenight.repo;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.gamenight.model.Game;

public interface GameRepository extends CrudRepository<Game, String>{

}
