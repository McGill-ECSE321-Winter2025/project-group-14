package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.GamenightApplication;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;

@SpringBootTest(classes = GamenightApplication.class)
public class GameOwnerRepositoryTest {
    
    @Autowired // everytime the test is called, @Autowired creates a new repo 
    private GameOwnerRepository gameOwnerRepository;

    @Autowired 
    private PersonRepository personRepository;

    @AfterEach
	public void clearDatabase() {
		gameOwnerRepository.deleteAll();
        personRepository.deleteAll();
	}

    @Test
    public void testCreateAndReadGameOwner() {
        String name = "Hamza";
        String emailAddress = "hamza@gmail.com";
        String password = "helloworld";
    
    
        Person person = new Person(emailAddress, password, name);
        person = personRepository.save(person);

        GameOwner hamza = new GameOwner(person);
        hamza.setActive(true);
        hamza = gameOwnerRepository.save(hamza);
        
        GameOwner hamzaFromDb = gameOwnerRepository.findById(hamza.getId()).orElse(null);

        assertNotNull(hamzaFromDb);
        assertNotNull(hamzaFromDb.getPerson());
        assertEquals(hamza.getPerson().getName(), hamzaFromDb.getPerson().getName());
        assertEquals(hamza.getPerson().getEmailAddress(), hamzaFromDb.getPerson().getEmailAddress());
        assertEquals(hamza.getPerson().getPassword(), hamzaFromDb.getPerson().getPassword());
        assertEquals(true, hamzaFromDb.isActive());

        hamzaFromDb.setActive(false);
        gameOwnerRepository.save(hamzaFromDb);
        
        GameOwner updatedHamza = gameOwnerRepository.findById(hamza.getId()).orElse(null);
        assertNotNull(updatedHamza);
        assertEquals(false, updatedHamza.isActive());
}
}