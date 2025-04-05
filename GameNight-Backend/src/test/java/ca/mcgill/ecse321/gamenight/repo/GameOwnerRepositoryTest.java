package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;

@SpringBootTest
@Transactional // Add this annotation
public class GameOwnerRepositoryTest {

    @Autowired
    private GameOwnerRepository gameOwnerRepository;

    @Autowired
    private PersonRepository personRepository;

    // @AfterEach is still okay, but Transactional handles rollback
    @AfterEach
    public void clearDatabase() {
        // Order is important if not using @Transactional fully reliably
        // GameOwner depends on Person
        gameOwnerRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    public void testCreateAndReadGameOwner() {
        String name = "Hamza";
        // Use a potentially unique email for testing if needed,
        // but @Transactional should prevent collisions now.
        String emailAddress = "hamza_owner@gmail.com"; 
        String password = "helloworld";

        Person person = new Person(emailAddress, password, name);
        person = personRepository.save(person);

        GameOwner hamza = new GameOwner(person);
        hamza.setActive(true);
        hamza = gameOwnerRepository.save(hamza);

        // It's often better to find by the ID of the entity you're testing
        // GameOwner hamzaFromDb = gameOwnerRepository.findByPersonId(person.getId());
        GameOwner hamzaFromDb = gameOwnerRepository.findById(hamza.getId()).orElse(null);


        assertNotNull(hamzaFromDb);
        assertNotNull(hamzaFromDb.getPerson());
        // Compare IDs for relationships if possible
        assertEquals(person.getId(), hamzaFromDb.getPerson().getId());
        assertEquals(name, hamzaFromDb.getPerson().getName());
        assertEquals(emailAddress, hamzaFromDb.getPerson().getEmailAddress());
        assertEquals(password, hamzaFromDb.getPerson().getPassword());
        assertEquals(true, hamzaFromDb.isActive());

        hamzaFromDb.setActive(false);
        gameOwnerRepository.save(hamzaFromDb);

        // GameOwner updatedHamza = gameOwnerRepository.findByPersonId(person.getId());
        GameOwner updatedHamza = gameOwnerRepository.findById(hamza.getId()).orElse(null);


        assertNotNull(updatedHamza);
        assertEquals(false, updatedHamza.isActive());
    }
}