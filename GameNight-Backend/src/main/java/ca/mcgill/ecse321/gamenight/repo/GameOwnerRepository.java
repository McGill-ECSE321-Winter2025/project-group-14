package ca.mcgill.ecse321.gamenight.repo;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.gamenight.model.GameOwner;

public interface GameOwnerRepository extends CrudRepository<GameOwner, Integer> {
    public GameOwner findByPersonId(int id);
}
