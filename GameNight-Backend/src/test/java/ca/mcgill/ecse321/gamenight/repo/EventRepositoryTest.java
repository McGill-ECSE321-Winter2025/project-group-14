package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.Event;

@SpringBootTest
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepo;

    @AfterEach
    public void clearDatabase() {
        eventRepo.deleteAll();
    }

    @Test
    public void testCreateAndRetrieveEvent() {
        Event event = new Event("Game Night", "Board games evening", new Date(), new Date());
        event = eventRepo.save(event);

        Optional<Event> fetchedEvent = eventRepo.findById(event.getId());

        assertNotNull(fetchedEvent.get());
        assertEquals(event.getName(), fetchedEvent.get().getName());
        assertEquals(event.getDescription(), fetchedEvent.get().getDescription());
        assertEquals(event.getStartTime(), fetchedEvent.get().getStartTime());
        assertEquals(event.getEndTime(), fetchedEvent.get().getEndTime());
    }
}