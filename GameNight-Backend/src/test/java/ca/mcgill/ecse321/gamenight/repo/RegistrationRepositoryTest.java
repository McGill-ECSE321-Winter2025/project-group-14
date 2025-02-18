package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.Registration;
import ca.mcgill.ecse321.gamenight.model.Registration.Key;

@SpringBootTest
public class RegistrationRepositoryTest {

    @Autowired
    private RegistrationRepository registrationRepo;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private PlayerRepository playerRepo;

    @AfterEach
    public void clearDatabase() {
        registrationRepo.deleteAll();
        eventRepo.deleteAll();
        playerRepo.deleteAll();
    }

    @Test
    public void testCreateRegistration() {
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 3600000L); // 1 hour later
        Event event = new Event("Test Event", "Test Event Description", startTime, endTime);
        event = eventRepo.save(event);

        Player player = new Player();
        player = playerRepo.save(player);

        Key key = new Key(player, event);
        Registration registration = new Registration(key);
        registration = registrationRepo.save(registration);

        Registration fetchedRegistration = registrationRepo.findByKey(registration.getKey());
        assertTrue(fetchedRegistration != null, "Registration should be present in repository");
        assertEquals(event.getId(), fetchedRegistration.getKey().getEvent().getId(), "Event IDs should match");
        assertEquals(player.getId(), fetchedRegistration.getKey().getPlayer().getId(), "Player IDs should match");
    }

    @Test
    public void testFindByEvent() {
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 3600000L);
        Event event = new Event("Group Event", "Event for group registration", startTime, endTime);
        event = eventRepo.save(event);

        Player player1 = new Player();
        player1 = playerRepo.save(player1);

        Player player2 = new Player();
        player2 = playerRepo.save(player2);

        Registration reg1 = new Registration(new Key(player1, event));
        Registration reg2 = new Registration(new Key(player2, event));
        registrationRepo.save(reg1);
        registrationRepo.save(reg2);

        List<Registration> regs = registrationRepo.findByKey_EventId(event.getId());
        assertNotNull(regs, "Registrations list should not be null");
        assertEquals(2, regs.size(), "There should be two registrations for the event");
    }

    @Test
    public void testFindByPlayer() {
        Player player = new Player();
        player = playerRepo.save(player);

        Date startTime1 = new Date();
        Date endTime1 = new Date(startTime1.getTime() + 3600000L);
        Event event1 = new Event("Morning Event", "Morning session", startTime1, endTime1);
        event1 = eventRepo.save(event1);

        Date startTime2 = new Date();
        Date endTime2 = new Date(startTime2.getTime() + 3600000L);
        Event event2 = new Event("Evening Event", "Evening session", startTime2, endTime2);
        event2 = eventRepo.save(event2);

        Registration reg1 = new Registration(new Key(player, event1));
        Registration reg2 = new Registration(new Key(player, event2));
        registrationRepo.save(reg1);
        registrationRepo.save(reg2);
        List<Registration> regs = registrationRepo.findByKey_PlayerId(player.getId());
        assertNotNull(regs, "Registrations list should not be null");
        assertEquals(2, regs.size(), "There should be two registrations for the player");
    }
}
