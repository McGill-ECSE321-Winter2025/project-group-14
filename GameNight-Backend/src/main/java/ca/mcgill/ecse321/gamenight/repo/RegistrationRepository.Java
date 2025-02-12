package ca.mcgill.ecse321.gamenight.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import ca.mcgill.ecse321.gamenight.model.Registration;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Player;

public interface RegistrationRepository extends CrudRepository<Registration, Integer> {

    List<Registration> findByEvent(Event event);

    List<Registration> findByPlayer(Player player);
}
