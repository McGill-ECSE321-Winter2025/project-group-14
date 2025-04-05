package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.Registration;
import ca.mcgill.ecse321.gamenight.model.Registration.Key;

@SpringBootTest
@Transactional // Add this annotation
public class RegistrationRepositoryTest {

    @Autowired
    private RegistrationRepository registrationRepo;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private PersonRepository personRepo;

    // @AfterEach is okay, but Transactional handles rollback
    @AfterEach
    public void clearDatabase() {
        // Correct order: Registration depends on Event and Player
        // Player depends on Person
        registrationRepo.deleteAll();
        eventRepo.deleteAll();
        playerRepo.deleteAll();
        personRepo.deleteAll();
    }

    @Test
    public void testCreateRegistration() {
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 3600000L); // 1 hour later
        Event event = new Event("Test Event", "Test Event Description", startTime, endTime);
        event = eventRepo.save(event);

        // Use unique emails if needed, @Transactional should help
        Person person = new Person("player1_reg@gmail.com", "password", "Player One");
        person = personRepo.save(person);

        Player player = new Player(person);
        player = playerRepo.save(player);

        Key key = new Key(player, event);
        Registration registration = new Registration(key);
        registration = registrationRepo.save(registration);

        // Use findById with the composite key
        Registration fetchedRegistration = registrationRepo.findById(registration.getKey()).orElse(null);
        assertNotNull(fetchedRegistration, "Registration should be present in repository");
        assertEquals(event.getId(), fetchedRegistration.getKey().getEvent().getId(), "Event IDs should match");
        assertEquals(player.getId(), fetchedRegistration.getKey().getPlayer().getId(), "Player IDs should match");
    }

    @Test
    public void testFindByEvent() {
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 3600000L);
        Event event = new Event("Group Event", "Event for group registration", startTime, endTime);
        event = eventRepo.save(event);

        // Use unique emails if needed, @Transactional should help
        Person person1 = new Person("player1_findevent@gmail.com", "password", "Player One");
        person1 = personRepo.save(person1);
        Player player1 = new Player(person1);
        player1 = playerRepo.save(player1);

        Person person2 = new Person("player2_findevent@gmail.com", "password", "Player Two");
        person2 = personRepo.save(person2);
        Player player2 = new Player(person2);
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
        // Use unique emails if needed, @Transactional should help
        Person person = new Person("player1_findplayer@gmail.com", "password", "Player One");
        person = personRepo.save(person);
        Player player = new Player(person);
        player = playerRepo.save(player);

        Date startTime1 = new Date();
        Date endTime1 = new Date(startTime1.getTime() + 3600000L);
        Event event1 = new Event("Morning Event", "Morning session", startTime1, endTime1);
        event1 = eventRepo.save(event1);

        // Ensure unique event times if necessary or use different names
        Date startTime2 = new Date(startTime1.getTime() + 7200000L); // 2 hours after first start
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