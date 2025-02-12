package ca.mcgill.ecse321.gamenight.repo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.Person;

@SpringBootTest
public class PersonRepositoryTest {
    @Autowired
    private PersonRepository personRepository;

    @AfterEach
    public void clearDatabase(){
        personRepository.deleteAll();
    }

    @Test
	public void testPersistAndLoadPerson() {
		String name = "Reina";
		String emailAddress = "reina@gmail.com";
		String password = "i_love_muffins";
		Person reina = new Person();
		reina.setName(name);
		reina.setEmailAddress(emailAddress);
		reina.setPassword(password); 
    }
}
