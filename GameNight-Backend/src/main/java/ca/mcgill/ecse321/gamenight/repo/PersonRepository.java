package ca.mcgill.ecse321.gamenight.repo;

import org.springframework.data.repository.CrudRepository;
import ca.mcgill.ecse321.gamenight.model.Person;

public interface PersonRepository extends CrudRepository<Person, Integer> {
    public Person findPersonById(int id);

    public Person findPersonByFirebaseUid(String firebaseUid); // Find by Firebase UID

    public Person findPersonByEmailAddress(String emailAddress); // Find by Email
}
