package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ca.mcgill.ecse321.gamenight.GamenightApplication;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;

@SpringBootTest(classes = GamenightApplication.class)
public class PlayerRepositoryTest {

    @Autowired // everytime the test is called, @Autowired creates a new repo
    private PlayerRepository playerRepository;
    @Autowired
    private PersonRepository personRepository;

    @AfterEach
    public void clearDatabase() {
        playerRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    public void testCreateAndReadPlayer() {
        String name = "Hamza";
        String emailAddress = "hamza@gmail.com";
        String password = "helloworld";

        Person person = new Person(emailAddress, password, name);
        person = personRepository.save(person);

        Player hamza = new Player(person);
        hamza = playerRepository.save(hamza);

        Player hamzaFromDb = playerRepository.findById(hamza.getId()).orElse(null);

        assertNotNull(hamzaFromDb);
        assertNotNull(hamzaFromDb.getPerson());
        assertEquals(hamza.getPerson().getName(), hamzaFromDb.getPerson().getName());
        assertEquals(hamza.getPerson().getEmailAddress(), hamzaFromDb.getPerson().getEmailAddress());
        assertEquals(hamza.getPerson().getPassword(), hamzaFromDb.getPerson().getPassword());

    }
}