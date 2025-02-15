package ca.mcgill.ecse321.gamenight.repo;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest
public class PlayerRepositoryTest {
    
    @Autowired // everytime the test is called, @Autowired creates a new repo 
    PlayerRepository playerRepository;

    @AfterEach
	public void clearDatabase() {
		playerRepository.deleteAll();
	}
}

