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

@SpringBootTest(classes = GamenightApplication.class)
@Transactional // Add this annotation
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    // @AfterEach is okay, but Transactional handles rollback
    @AfterEach
    public void clearDatabase() {
        personRepository.deleteAll();
    }

    @Test
    public void testCreateAndReadPerson() {
        String name = "Reina";
        // Use a potentially unique email for testing if needed,
        // but @Transactional should prevent collisions now.
        String emailAddress = "reina_person@gmail.com"; 
        String password = "i_love_muffins";

        Person reina = new Person();
        reina.setName(name);
        reina.setEmailAddress(emailAddress);
        reina.setPassword(password);
        reina = personRepository.save(reina);

        // Use findById which returns Optional
        Person reinaFromDb = personRepository.findById(reina.getId()).orElse(null);

        assertNotNull(reinaFromDb);
        assertEquals(reina.getName(), reinaFromDb.getName());
        assertEquals(reina.getEmailAddress(), reinaFromDb.getEmailAddress());
        assertEquals(reina.getPassword(), reinaFromDb.getPassword());
    }
}