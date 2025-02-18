package ca.mcgill.ecse321.gamenight.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import ca.mcgill.ecse321.gamenight.model.Registration;

public interface RegistrationRepository extends CrudRepository<Registration, Registration.Key> {

    Registration findByKey(Registration.Key key);

    List<Registration> findByKey_EventId(int eventId);

    List<Registration> findByKey_PlayerId(int playerId);
}
