package ca.mcgill.ecse321.gamenight.repo;

import org.springframework.data.repository.CrudRepository;
import ca.mcgill.ecse321.gamenight.model.Event;

public interface EventRepository extends CrudRepository<Event, Integer> {

}