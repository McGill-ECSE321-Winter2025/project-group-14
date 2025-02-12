package ca.mcgill.ecse321.gamenight.repo;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GameOwnerRepositoryTest {
    
    @Autowired // everytime the test is called, @Autowired creates a new repo 
    GameOwnerRepository gameOwner;

    @AfterEach
	public void clearDatabase() {
		gameOwner.deleteAll();
	}
}
