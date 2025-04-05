package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date; // Use java.sql.Date if your model uses it
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach; // Use BeforeEach for setup logic run before each test
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;

@SpringBootTest
@Transactional // <--- Add Transactional for test isolation
public class BorrowingRequestRepositoryTest {

    @Autowired
    private BorrowingRequestRepository borrowingRequestRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private GameCopyRepository gameCopyRepository;
    @Autowired
    private GameOwnerRepository gameOwnerRepository;
    @Autowired
    private GameRepository gameRepository;

    // It's generally better to clean up *after* each test
    @AfterEach
    public void clearDatabase() {
        // Ensure correct deletion order (Dependents first)
        borrowingRequestRepository.deleteAll();
        // Assuming GameCopy depends on Game and GameOwner
        gameCopyRepository.deleteAll();
        // Assuming Player and GameOwner depend on Person
        playerRepository.deleteAll();
        gameOwnerRepository.deleteAll();
        // Delete the dependencies last
        personRepository.deleteAll();
        gameRepository.deleteAll();
    }

    // Helper method to create a valid persisted Player
    private Player createAndSaveTestPlayer(String email, String name) {
        Person person = new Person(email, "password", name);
        Person savedPerson = personRepository.save(person); // Save Person FIRST
        Player player = new Player();
        player.setPerson(savedPerson); // Link the SAVED Person
        return playerRepository.save(player); // Save Player AFTER linking
    }

    // Helper method to create a valid persisted GameCopy
    private GameCopy createAndSaveTestGameCopy(String gameName, String ownerEmail, String ownerName) {
        Person ownerPerson = new Person(ownerEmail, "password", ownerName);
        Person savedOwnerPerson = personRepository.save(ownerPerson); // Save Owner Person FIRST
        GameOwner owner = new GameOwner();
        owner.setPerson(savedOwnerPerson); // Link the SAVED Person
        GameOwner savedOwner = gameOwnerRepository.save(owner); // Save GameOwner

        Game game = new Game();
        game.setName(gameName);
        Game savedGame = gameRepository.save(game); // Save Game

        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(savedGame); // Link saved Game
        gameCopy.setGameOwner(savedOwner); // Link saved GameOwner
        return gameCopyRepository.save(gameCopy); // Save GameCopy
    }


    @Test
    public void testCreateAndReadBorrowingRequest() {
        // --- Setup ---
        Player sender = createAndSaveTestPlayer("sender@test.com", "Sender Name");
        GameCopy gameCopy = createAndSaveTestGameCopy("Test Game", "owner@test.com", "Owner Name");

        Date sendTime = new Date(System.currentTimeMillis());
        Date startTime = Date.valueOf("2025-05-01");
        Date endTime = Date.valueOf("2025-05-10");

        BorrowingRequest request = new BorrowingRequest();
        request.setSender(sender); // Link saved Player
        request.setGameCopy(gameCopy); // Link saved GameCopy
        request.setSendTime(sendTime);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(BorrowingRequestStatus.Delivered);

        // --- Action ---
        BorrowingRequest savedRequest = borrowingRequestRepository.save(request);

        // --- Assert ---
        assertNotNull(savedRequest);
        assertNotNull(savedRequest.getId()); // Should have an ID after saving

        Optional<BorrowingRequest> retrievedRequestOpt = borrowingRequestRepository.findById(savedRequest.getId());
        assertTrue(retrievedRequestOpt.isPresent());
        BorrowingRequest retrievedRequest = retrievedRequestOpt.get();

        assertNotNull(retrievedRequest.getSender());
        assertNotNull(retrievedRequest.getGameCopy());
        assertEquals(sender.getId(), retrievedRequest.getSender().getId());
        assertEquals(gameCopy.getId(), retrievedRequest.getGameCopy().getId());
        assertEquals(BorrowingRequestStatus.Delivered, retrievedRequest.getStatus());
        assertEquals(startTime, retrievedRequest.getStartTime());
        assertEquals(endTime, retrievedRequest.getEndTime());
        assertEquals(sendTime.getTime(), retrievedRequest.getSendTime().getTime());
    }

    @Test
    public void testUpdateBorrowingRequest() {
         // --- Setup ---
        Player sender = createAndSaveTestPlayer("sender_update@test.com", "Update Sender");
        GameCopy gameCopy = createAndSaveTestGameCopy("Update Game", "owner_update@test.com", "Update Owner");
        Date sendTime = new Date(System.currentTimeMillis());
        Date startTime = Date.valueOf("2025-06-01");
        Date endTime = Date.valueOf("2025-06-10");

        BorrowingRequest request = new BorrowingRequest();
        request.setSender(sender);
        request.setGameCopy(gameCopy);
        request.setSendTime(sendTime);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(BorrowingRequestStatus.Delivered); // Initial status
        BorrowingRequest savedRequest = borrowingRequestRepository.save(request);
        assertNotNull(savedRequest.getId());

        // --- Action ---
        Optional<BorrowingRequest> requestToUpdateOpt = borrowingRequestRepository.findById(savedRequest.getId());
        assertTrue(requestToUpdateOpt.isPresent());
        BorrowingRequest requestToUpdate = requestToUpdateOpt.get();

        requestToUpdate.setStatus(BorrowingRequestStatus.Accepted); // Change the status
        BorrowingRequest updatedRequest = borrowingRequestRepository.save(requestToUpdate); // Save the change

        // --- Assert ---
        assertNotNull(updatedRequest);
        assertEquals(savedRequest.getId(), updatedRequest.getId()); // ID should remain the same
        assertEquals(BorrowingRequestStatus.Accepted, updatedRequest.getStatus()); // Status should be updated

        Optional<BorrowingRequest> finalCheckOpt = borrowingRequestRepository.findById(savedRequest.getId());
        assertTrue(finalCheckOpt.isPresent());
        assertEquals(BorrowingRequestStatus.Accepted, finalCheckOpt.get().getStatus());
    }

     @Test
    public void testFindBySender() {
        // --- Setup ---
        Player sender1 = createAndSaveTestPlayer("sender1@find.com", "Sender One");
        Player sender2 = createAndSaveTestPlayer("sender2@find.com", "Sender Two");
        GameCopy gameCopy = createAndSaveTestGameCopy("Find Game", "owner@find.com", "Find Owner");

        // --- FIX: Use default constructor and setters ---
        BorrowingRequest req1 = new BorrowingRequest();
        req1.setSender(sender1);
        req1.setGameCopy(gameCopy);
        req1.setSendTime(new Date(System.currentTimeMillis()));
        req1.setStartTime(Date.valueOf("2025-07-01"));
        req1.setEndTime(Date.valueOf("2025-07-10"));
        req1.setStatus(BorrowingRequestStatus.Delivered);

        BorrowingRequest req2 = new BorrowingRequest();
        req2.setSender(sender2);
        req2.setGameCopy(gameCopy);
        req2.setSendTime(new Date(System.currentTimeMillis()));
        req2.setStartTime(Date.valueOf("2025-08-01"));
        req2.setEndTime(Date.valueOf("2025-08-10"));
        req2.setStatus(BorrowingRequestStatus.Accepted);

        BorrowingRequest req3 = new BorrowingRequest();
        req3.setSender(sender1);
        req3.setGameCopy(gameCopy);
        req3.setSendTime(new Date(System.currentTimeMillis()));
        req3.setStartTime(Date.valueOf("2025-09-01"));
        req3.setEndTime(Date.valueOf("2025-09-10"));
        req3.setStatus(BorrowingRequestStatus.Rejected);
        // --- End of FIX ---

        borrowingRequestRepository.save(req1);
        borrowingRequestRepository.save(req2);
        borrowingRequestRepository.save(req3);

        // --- Action ---
        List<BorrowingRequest> sender1Requests = borrowingRequestRepository.findAllBySenderId(sender1.getId());

        // --- Assert ---
        assertNotNull(sender1Requests);
        assertEquals(2, sender1Requests.size());
        assertTrue(sender1Requests.stream().anyMatch(r -> r.getStatus() == BorrowingRequestStatus.Delivered));
        assertTrue(sender1Requests.stream().anyMatch(r -> r.getStatus() == BorrowingRequestStatus.Rejected));
    }

}