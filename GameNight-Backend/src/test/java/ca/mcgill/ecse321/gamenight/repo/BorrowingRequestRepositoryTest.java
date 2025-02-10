package ca.mcgill.ecse321.gamenight.repo;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BorrowingRequestRepositoryTest {

    @Autowired
    BorrowingRequestRepository repo;

    @AfterEach
	public void clearDatabase() {
		repo.deleteAll();
	}
}
