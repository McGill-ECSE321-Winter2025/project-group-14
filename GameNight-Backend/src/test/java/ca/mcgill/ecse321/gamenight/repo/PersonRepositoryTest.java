package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.GamenightApplication;
import ca.mcgill.ecse321.gamenight.model.Person;

@SpringBootTest(classes = GamenightApplication.class)
public class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @AfterEach
    public void clearDatabase() {
        personRepository.deleteAll();
    }

    @Test
    public void testCreateAndReadPerson() {
        // Given
        String name = "Reina";
        String emailAddress = "reina@gmail.com";
        String password = "i_love_muffins";
        String firebaseUid = "firebase-uid-12345"; // Required Firebase UID

        // When
        Person reina = new Person(emailAddress, password, name, firebaseUid);
        reina = personRepository.save(reina);

        // Then
        Person reinaFromDb = personRepository.findPersonById(reina.getId());

        assertNotNull(reinaFromDb);
        assertEquals(reina.getName(), reinaFromDb.getName());
        assertEquals(reina.getEmailAddress(), reinaFromDb.getEmailAddress());
        assertEquals(reina.getPassword(), reinaFromDb.getPassword());
        assertEquals(reina.getFirebaseUid(), reinaFromDb.getFirebaseUid()); // New check for Firebase UID
    }
}
