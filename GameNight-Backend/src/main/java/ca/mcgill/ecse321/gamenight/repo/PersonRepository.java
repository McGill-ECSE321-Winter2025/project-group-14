package ca.mcgill.ecse321.gamenight.repo;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import ca.mcgill.ecse321.gamenight.model.Person;

public interface PersonRepository extends CrudRepository<Person, Integer> {
    public Optional<Person> findPersonById(int id);

    public Optional<Person> findPersonByEmailAddress(String email);
}
