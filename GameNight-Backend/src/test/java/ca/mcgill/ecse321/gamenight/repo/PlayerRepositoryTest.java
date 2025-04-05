package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import ca.mcgill.ecse321.gamenight.GamenightApplication;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;

@SpringBootTest(classes = GamenightApplication.class)
@Transactional // Add this annotation
public class PlayerRepositoryTest {

    @Autowired 
    private PlayerRepository playerRepository;
    @Autowired
    private PersonRepository personRepository;

    // @AfterEach is okay, but Transactional handles rollback
    @AfterEach
    public void clearDatabase() {
        // Order is important: Player depends on Person
        playerRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    public void testCreateAndReadPlayer() {
        String name = "Hamza";
        // Use a potentially unique email for testing if needed,
        // but @Transactional should prevent collisions now.
        String emailAddress = "hamza_player@gmail.com"; 
        String password = "helloworld";

        Person person = new Person(emailAddress, password, name);
        person = personRepository.save(person);

        Player hamza = new Player(person);
        hamza = playerRepository.save(hamza);

        // Find by Player ID is generally better here
        // Player hamzaFromDb = playerRepository.findByPersonId(person.getId());
        Player hamzaFromDb = playerRepository.findById(hamza.getId()).orElse(null);


        assertNotNull(hamzaFromDb);
        assertNotNull(hamzaFromDb.getPerson());
        // Compare IDs for relationships if possible
        assertEquals(person.getId(), hamzaFromDb.getPerson().getId());
        assertEquals(name, hamzaFromDb.getPerson().getName());
        assertEquals(emailAddress, hamzaFromDb.getPerson().getEmailAddress());
        assertEquals(password, hamzaFromDb.getPerson().getPassword());
    }
}