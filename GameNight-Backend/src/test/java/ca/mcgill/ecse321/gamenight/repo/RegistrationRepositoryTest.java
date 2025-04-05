package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional; 

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional; 

import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.Registration;
import ca.mcgill.ecse321.gamenight.model.Registration.Key;

@SpringBootTest
@Transactional 
public class RegistrationRepositoryTest {

    @Autowired
    private RegistrationRepository registrationRepo;

    @Autowired
    private EventRepository eventRepo;

    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private PersonRepository personRepo;

    @AfterEach
    public void clearDatabase() {
        registrationRepo.deleteAll();
        eventRepo.deleteAll();
        personRepo.deleteAll();
        playerRepo.deleteAll();
    }

    private Player createAndSaveTestPlayer(String email, String name) {
        Person person = new Person(email, "password", name);
        Person savedPerson = personRepo.save(person); 
        Player player = new Player(); 
        player.setPerson(savedPerson); 
        return playerRepo.save(player); 
    }


    @Test
    public void testCreateRegistration() {
        long nowMillis = System.currentTimeMillis();
        java.sql.Date startTime = new java.sql.Date(nowMillis);
        java.sql.Date endTime = new java.sql.Date(nowMillis + 3600000L); 

        Event event = new Event("Test Event Reg", "Test Event Desc Reg", startTime, endTime);
        event = eventRepo.save(event);

        Player player = createAndSaveTestPlayer("player1_reg@gmail.com", "Player One Reg");

        Key key = new Key(player, event); 
        Registration registration = new Registration(key);
        registration = registrationRepo.save(registration);

        Optional<Registration> fetchedRegistrationOpt = registrationRepo.findById(registration.getKey());
        assertTrue(fetchedRegistrationOpt.isPresent(), "Registration should be present in repository");
        Registration fetchedRegistration = fetchedRegistrationOpt.get();

        assertEquals(event.getId(), fetchedRegistration.getKey().getEvent().getId(), "Event IDs should match");
        assertEquals(player.getId(), fetchedRegistration.getKey().getPlayer().getId(), "Player IDs should match");
    }

    @Test
    public void testFindByEvent() {
        long nowMillis = System.currentTimeMillis();
        java.sql.Date startTime = new java.sql.Date(nowMillis);
        java.sql.Date endTime = new java.sql.Date(nowMillis + 3600000L);
        Event event = new Event("Group Event Find", "Event find by event", startTime, endTime);
        event = eventRepo.save(event);

        Player player1 = createAndSaveTestPlayer("player1_findevent@gmail.com", "Player One FindEvent");
        Player player2 = createAndSaveTestPlayer("player2_findevent@gmail.com", "Player Two FindEvent");

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
        Player player = createAndSaveTestPlayer("player1_findplayer@gmail.com", "Player One FindPlayer");

        long nowMillis = System.currentTimeMillis();
        java.sql.Date startTime1 = new java.sql.Date(nowMillis);
        java.sql.Date endTime1 = new java.sql.Date(nowMillis + 3600000L);
        Event event1 = new Event("Morning Event Find", "Morning session find", startTime1, endTime1);
        event1 = eventRepo.save(event1);

        java.sql.Date startTime2 = new java.sql.Date(nowMillis + 7200000L); 
        java.sql.Date endTime2 = new java.sql.Date(startTime2.getTime() + 3600000L);
        Event event2 = new Event("Evening Event Find", "Evening session find", startTime2, endTime2);
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