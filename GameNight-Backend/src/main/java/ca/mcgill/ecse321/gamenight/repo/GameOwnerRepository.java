package ca.mcgill.ecse321.gamenight.repo;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;

public interface GameOwnerRepository extends CrudRepository<GameOwner, Integer> {
    public GameOwner findGameOwnerById(int id);

    void deleteAllByPerson(Person person);

}
