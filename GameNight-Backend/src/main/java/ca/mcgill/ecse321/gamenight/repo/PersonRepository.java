package ca.mcgill.ecse321.gamenight.repo;

import org.springframework.data.repository.CrudRepository;
import ca.mcgill.ecse321.gamenight.model.Person;

public interface PersonRepository extends CrudRepository<Person, String> {
}
