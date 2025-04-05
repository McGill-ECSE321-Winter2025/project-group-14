package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
@SpringBootTest
@Transactional
public class PlayerRepositoryTest {

    @Autowired 
    private PlayerRepository playerRepository;
    @Autowired
    private PersonRepository personRepository;

    @Test
    public void testCreateAndReadPlayer() {
        String name = "Hamza";
        String emailAddress = "hamza_player_test@gmail.com"; 
        String password = "helloworld";
        Person person = new Person(emailAddress, password, name);
        Person savedPerson = personRepository.save(person);
        assertNotNull(savedPerson, "Saved person should not be null");
        assertNotNull(savedPerson.getId(), "Saved person should have an ID");
        Player hamzaPlayer = new Player(); 
        hamzaPlayer.setPerson(savedPerson); 
        Player savedPlayer = playerRepository.save(hamzaPlayer);
        assertNotNull(savedPlayer, "Saved player should not be null");
        assertNotNull(savedPlayer.getId(), "Saved player should have an ID");
        Optional<Player> hamzaFromDbOpt = playerRepository.findById(savedPlayer.getId());
        assertTrue(hamzaFromDbOpt.isPresent(), "Player not found in DB after save");
        Player hamzaFromDb = hamzaFromDbOpt.get();

        assertNotNull(hamzaFromDb.getPerson(), "Person associated with player should not be null");
        assertEquals(savedPerson.getId(), hamzaFromDb.getPerson().getId(), "Person ID mismatch");
        assertEquals(name, hamzaFromDb.getPerson().getName());
        assertEquals(emailAddress, hamzaFromDb.getPerson().getEmailAddress());
        assertEquals(password, hamzaFromDb.getPerson().getPassword());
    }
    
}