package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date; // Use java.sql.Date if your model uses it
import java.util.List;
import java.util.Optional;

// Imports for debugging clearDatabase
import java.util.ArrayList; // Needed if using List explicitly

import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach; // Use BeforeEach for setup logic run before each test
import org.junit.jupiter.api.Test;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
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
@Transactional // Add Transactional for test isolation
public class BorrowingRequestRepositoryTest {

    // Logger for debugging
    private static final Logger log = LoggerFactory.getLogger(BorrowingRequestRepositoryTest.class);

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

    /**
     * DEBUGGING VERSION of clearDatabase.
     * Iterates through Players to help identify which one causes the PropertyValueException.
     * REMEMBER TO REVERT TO USING deleteAll() after debugging.
     */
    @AfterEach
    public void clearDatabase() {
        log.info("--- Clearing Database ---");
        // Delete dependents first
        borrowingRequestRepository.deleteAll();
        log.info("Deleted BorrowingRequests");
        gameCopyRepository.deleteAll();
        log.info("Deleted GameCopies");

        // Debug Player deletion
        log.info("Attempting to delete Players individually for debugging...");
        List<Player> playersToDelete = new ArrayList<>(); // Use ArrayList for modifiable list if needed later
        playerRepository.findAll().forEach(playersToDelete::add); // Populate the list safely
        log.info("Found {} players to delete.", playersToDelete.size());

        for (Player player : playersToDelete) {
            try {
                Integer playerId = player.getId(); // Assume ID is Integer or adjust type
                String personInfo = "NULL";
                if (player.getPerson() != null) {
                    Integer personId = player.getPerson().getId(); // Adjust ID type if needed
                     // Check if person has ID - indicates if likely persisted
                     if (personId != null) {
                         personInfo = "ID: " + personId;
                     } else {
                         personInfo = "Object exists but has NULL ID (transient?)";
                     }
                } else {
                    log.error("!!! Player ID {} has NULL person before delete attempt!", playerId);
                }
                log.info("Attempting deletion of Player ID {} with Person Info: {}", playerId, personInfo);

                playerRepository.delete(player); // Attempt individual deletion

                log.info("Successfully deleted Player ID {}", playerId);
            } catch (Exception e) {
                log.error("!!! FAILED to delete Player ID {} !!!", player.getId(), e); // Log exception stack trace
                // Optionally re-throw to ensure test failure indication, though logs might be enough for debug
                // throw new RuntimeException("Failed during player deletion debug for Player ID: " + player.getId(), e);
            }
        }
        log.info("Finished Player deletion attempt.");

        // Delete remaining entities in correct order
        gameOwnerRepository.deleteAll();
        log.info("Deleted GameOwners");
        personRepository.deleteAll();
        log.info("Deleted Persons");
        gameRepository.deleteAll();
        log.info("Deleted Games");
        log.info("--- Database Clearing Finished ---");
    }

    // Helper method to create a valid persisted Player
    private Player createAndSaveTestPlayer(String email, String name) {
        // Check if person already exists to avoid duplicate emails within a single non-transactional setup (if needed)
        // Optional<Person> existing = personRepository.findPersonByEmailAddress(email);
        // if (existing.isPresent()) return playerRepository.findByPersonId(existing.get().getId()); // Or handle differently

        Person person = new Person(email, "password", name);
        Person savedPerson = personRepository.save(person); // Save Person FIRST
        Player player = new Player();
        player.setPerson(savedPerson); // Link the SAVED Person
        return playerRepository.save(player); // Save Player AFTER linking
    }

    // Helper method to create a valid persisted GameCopy
    private GameCopy createAndSaveTestGameCopy(String gameName, String ownerEmail, String ownerName) {
        Person ownerPerson = new Person(ownerEmail, "password", ownerName);
        Person savedOwnerPerson = personRepository.save(ownerPerson); 
        GameOwner owner = new GameOwner();
        owner.setPerson(savedOwnerPerson); 
        GameOwner savedOwner = gameOwnerRepository.save(owner); 

        // --- FIX: Use the defined findGameByName and handle Optional ---
        Optional<Game> existingGameOpt = gameRepository.findGameByName(gameName); // Use the new method
        Game savedGame;

        if (existingGameOpt.isPresent()) {
            savedGame = existingGameOpt.get(); // Use existing game if found
             log.info("Found existing game: {}", gameName);
        } else {
            Game game = new Game();
            game.setName(gameName);
            savedGame = gameRepository.save(game); // Save new Game if not found
            log.info("Created new game: {}", gameName);
        }
        // --- End of FIX ---

        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(savedGame); // Link saved Game
        gameCopy.setGameOwner(savedOwner); // Link saved GameOwner
        return gameCopyRepository.save(gameCopy); // Save GameCopy
    }

    @Test
    public void testCreateAndReadBorrowingRequest() {
        // --- Setup ---
        Player sender = createAndSaveTestPlayer("sender_cr@test.com", "Sender CR Name");
        GameCopy gameCopy = createAndSaveTestGameCopy("Test Game CR", "owner_cr@test.com", "Owner CR Name");

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
        assertNotNull(savedRequest.getId());

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
        // Use unique emails to potentially avoid side effects if Transactional isn't perfect
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
        log.info("Created BorrowingRequest ID: {} for testUpdateBorrowingRequest", savedRequest.getId());


        // --- Action ---
        Optional<BorrowingRequest> requestToUpdateOpt = borrowingRequestRepository.findById(savedRequest.getId());
        assertTrue(requestToUpdateOpt.isPresent(), "Request to update was not found in DB");
        BorrowingRequest requestToUpdate = requestToUpdateOpt.get();

        log.info("Updating status for BorrowingRequest ID: {}", requestToUpdate.getId());
        requestToUpdate.setStatus(BorrowingRequestStatus.Accepted); // Change the status
        BorrowingRequest updatedRequest = borrowingRequestRepository.save(requestToUpdate); // Save the change
        log.info("Saved updated BorrowingRequest ID: {}", updatedRequest.getId());


        // --- Assert ---
        assertNotNull(updatedRequest);
        assertEquals(savedRequest.getId(), updatedRequest.getId());
        assertEquals(BorrowingRequestStatus.Accepted, updatedRequest.getStatus());

        // Verify by fetching again
        Optional<BorrowingRequest> finalCheckOpt = borrowingRequestRepository.findById(savedRequest.getId());
        assertTrue(finalCheckOpt.isPresent(), "Request not found in DB after update");
        assertEquals(BorrowingRequestStatus.Accepted, finalCheckOpt.get().getStatus());
        log.info("Verified updated status for BorrowingRequest ID: {}", finalCheckOpt.get().getId());

    }

     @Test
    public void testFindBySender() {
        // --- Setup ---
         // Use unique emails
        Player sender1 = createAndSaveTestPlayer("sender1@find.com", "Sender One");
        Player sender2 = createAndSaveTestPlayer("sender2@find.com", "Sender Two");
        GameCopy gameCopy = createAndSaveTestGameCopy("Find Game", "owner@find.com", "Find Owner");

        // Use default constructor and setters
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

        borrowingRequestRepository.save(req1);
        borrowingRequestRepository.save(req2);
        borrowingRequestRepository.save(req3);
        log.info("Created requests for testFindBySender");


        // --- Action ---
        log.info("Finding requests for sender ID: {}", sender1.getId());
        List<BorrowingRequest> sender1Requests = borrowingRequestRepository.findAllBySenderId(sender1.getId());
        log.info("Found {} requests for sender ID: {}", sender1Requests.size(), sender1.getId());


        // --- Assert ---
        assertNotNull(sender1Requests);
        assertEquals(2, sender1Requests.size());
        assertTrue(sender1Requests.stream().anyMatch(r -> r.getStatus() == BorrowingRequestStatus.Delivered));
        assertTrue(sender1Requests.stream().anyMatch(r -> r.getStatus() == BorrowingRequestStatus.Rejected));
    }

}