package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.Registration;

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

        Registration registration = new Registration(event, player);
        registration = registrationRepo.save(registration);

        Optional<Registration> fetchedRegistrationOpt = registrationRepo.findById(registration.getId());
        assertTrue(fetchedRegistrationOpt.isPresent(), "Registration should be present in repository");
        Registration fetchedRegistration = fetchedRegistrationOpt.get();
        assertEquals(event.getId(), fetchedRegistration.getEvent().getId(), "Event IDs should match");
        assertEquals(player.getId(), fetchedRegistration.getPlayer().getId(), "Player IDs should match");
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

        Registration reg1 = new Registration(event, player1);
        Registration reg2 = new Registration(event, player2);
        registrationRepo.save(reg1);
        registrationRepo.save(reg2);

        List<Registration> regs = registrationRepo.findByEvent(event);
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

        Registration reg1 = new Registration(event1, player);
        Registration reg2 = new Registration(event2, player);
        registrationRepo.save(reg1);
        registrationRepo.save(reg2);

        List<Registration> regs = registrationRepo.findByPlayer(player);
        assertNotNull(regs, "Registrations list should not be null");
        assertEquals(2, regs.size(), "There should be two registrations for the player");
    }
}
