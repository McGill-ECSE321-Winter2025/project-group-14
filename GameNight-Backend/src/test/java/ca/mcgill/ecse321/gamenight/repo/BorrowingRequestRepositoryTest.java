package ca.mcgill.ecse321.gamenight.repo;

import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BorrowingRequestRepositoryTest {

    @Autowired
    private BorrowingRequestRepository borrowingRepo;

    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private GameCopyRepository gameCopyRepo;

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private GameOwnerRepository gameOwnerRepo;

    @Autowired
    private GameRepository gameRepo;

    @AfterEach
    public void clearDatabase() {
        borrowingRepo.deleteAll();
        playerRepo.deleteAll();
        gameCopyRepo.deleteAll();
        gameOwnerRepo.deleteAll();
        gameRepo.deleteAll();
        personRepo.deleteAll();
    }

    @Test
    public void testCreateAndReadBorrowingRequest() {
        // Create an owner
        Person person1 = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person1);

        GameOwner owner = new GameOwner();
        owner.setPerson(person1);
        gameOwnerRepo.save(owner);

        // Create a borrower
        Person person2 = new Person("bbbbbb@gmail.com", "bbbbb", "Patrick");
        personRepo.save(person2);

        Player borrower = new Player();
        borrower.setPerson(person2);
        playerRepo.save(borrower);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Create a BorrowingRequest
        Date startTime = Date.valueOf("2023-10-01");
        Date endTime = Date.valueOf("2023-10-10");
        BorrowingRequest request = new BorrowingRequest(startTime, endTime, borrower, gameCopy);
        borrowingRepo.save(request);

        // Read the BorrowingRequest
        BorrowingRequest retrievedRequest = borrowingRepo.findById(request.getId()).orElse(null);

        // Assertions
        assertNotNull(retrievedRequest);
        assertEquals(startTime, retrievedRequest.getStartTime());
        assertEquals(endTime, retrievedRequest.getEndTime());
        assertEquals(BorrowingRequestStatus.Delivered, retrievedRequest.getStatus());
        assertEquals(borrower.getPerson().getEmailAddress(),
                retrievedRequest.getSender().getPerson().getEmailAddress());
        assertEquals(gameCopy.getId(), retrievedRequest.getGameCopy().getId());
    }

    @Test
    public void testUpdateBorrowingRequest() {
        // Create an owner
        Person person1 = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person1);

        GameOwner owner = new GameOwner();
        owner.setPerson(person1);
        gameOwnerRepo.save(owner);

        // Create a borrower
        Person person2 = new Person("bbbbbb@gmail.com", "bbbbb", "Patrick");
        personRepo.save(person2);

        Player borrower = new Player();
        borrower.setPerson(person2);
        playerRepo.save(borrower);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Create a BorrowingRequest
        Date startTime = Date.valueOf("2023-10-01");
        Date endTime = Date.valueOf("2023-10-10");
        BorrowingRequest request = new BorrowingRequest(startTime, endTime, borrower, gameCopy);
        borrowingRepo.save(request);

        // Update the BorrowingRequest
        request.setStatus(BorrowingRequestStatus.Accepted);
        borrowingRepo.save(request);

        // Retrieve the updated BorrowingRequest
        BorrowingRequest updatedRequest = borrowingRepo.findById(request.getId()).orElse(null);

        // Assertions
        assertNotNull(updatedRequest);
        assertEquals(BorrowingRequestStatus.Accepted, updatedRequest.getStatus());
    }

    @Test
    public void testDeleteBorrowingRequest() {
        // Create an owner
        Person person1 = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person1);

        GameOwner owner = new GameOwner();
        owner.setPerson(person1);
        gameOwnerRepo.save(owner);

        // Create a borrower
        Person person2 = new Person("bbbbbb@gmail.com", "bbbbb", "Patrick");
        personRepo.save(person2);

        Player borrower = new Player();
        borrower.setPerson(person2);
        playerRepo.save(borrower);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Create a BorrowingRequest
        Date startTime = Date.valueOf("2023-10-01");
        Date endTime = Date.valueOf("2023-10-10");
        BorrowingRequest request = new BorrowingRequest(startTime, endTime, borrower, gameCopy);
        borrowingRepo.save(request);

        // Delete the BorrowingRequest
        borrowingRepo.delete(request);

        // Try to retrieve the deleted BorrowingRequest
        BorrowingRequest deletedRequest = borrowingRepo.findById(request.getId()).orElse(null);

        // Assertions
        assertNull(deletedRequest);
    }

    @Test
    public void testFindAllBorrowingRequests() {
        // Create an owner
        Person person1 = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person1);

        GameOwner owner = new GameOwner();
        owner.setPerson(person1);
        gameOwnerRepo.save(owner);

        // Create a borrower
        Person person2 = new Person("bbbbbb@gmail.com", "bbbbb", "Patrick");
        personRepo.save(person2);

        Player borrower = new Player();
        borrower.setPerson(person2);
        playerRepo.save(borrower);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Create multiple BorrowingRequests
        BorrowingRequest request1 = new BorrowingRequest(Date.valueOf("2023-10-01"), Date.valueOf("2023-10-10"),
                borrower, gameCopy);
        BorrowingRequest request2 = new BorrowingRequest(Date.valueOf("2023-11-01"), Date.valueOf("2023-11-10"),
                borrower, gameCopy);
        borrowingRepo.save(request1);
        borrowingRepo.save(request2);

        // Retrieve all BorrowingRequests
        List<BorrowingRequest> requests = (List<BorrowingRequest>) borrowingRepo.findAll();

        // Assertions
        assertEquals(2, requests.size());
    }

    @Test
    public void testFindAllRequestsByStatusAndGameOwner() {
        // Create an owner
        Person person1 = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person1);

        GameOwner owner = new GameOwner();
        owner.setPerson(person1);
        gameOwnerRepo.save(owner);

        // Create a borrower
        Person person2 = new Person("bbbbbb@gmail.com", "bbbbb", "Patrick");
        personRepo.save(person2);

        Player borrower = new Player();
        borrower.setPerson(person2);
        playerRepo.save(borrower);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Create BorrowingRequests with different statuses
        BorrowingRequest request1 = new BorrowingRequest(Date.valueOf("2023-10-01"), Date.valueOf("2023-10-10"),
                borrower, gameCopy);
        request1.setStatus(BorrowingRequestStatus.Rejected);
        borrowingRepo.save(request1);

        BorrowingRequest request2 = new BorrowingRequest(Date.valueOf("2023-11-01"), Date.valueOf("2023-11-10"),
                borrower, gameCopy);
        request2.setStatus(BorrowingRequestStatus.Accepted);
        borrowingRepo.save(request2);

        // Retrieve requests by status and game owner
        List<BorrowingRequest> pendingRequests = borrowingRepo.findAllRequestsByStatusAndGameOwner(
                BorrowingRequestStatus.Rejected,
                owner.getId());

        // Assertions
        assertEquals(1, pendingRequests.size());
        assertEquals(request1.getId(), pendingRequests.get(0).getId());
    }

    @Test
    public void testFindBySender() {
        // Create an owner
        Person person1 = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person1);

        GameOwner owner = new GameOwner();
        owner.setPerson(person1);
        gameOwnerRepo.save(owner);

        // Create a borrower
        Person person2 = new Person("bbbbbb@gmail.com", "bbbbb", "Patrick");
        personRepo.save(person2);

        Player borrower = new Player();
        borrower.setPerson(person2);
        playerRepo.save(borrower);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Create BorrowingRequests for the same sender
        BorrowingRequest request1 = new BorrowingRequest(Date.valueOf("2023-10-01"), Date.valueOf("2023-10-10"),
                borrower, gameCopy);
        BorrowingRequest request2 = new BorrowingRequest(Date.valueOf("2023-11-01"), Date.valueOf("2023-11-10"),
                borrower, gameCopy);
        borrowingRepo.save(request1);
        borrowingRepo.save(request2);

        // Retrieve requests by sender
        List<BorrowingRequest> requestsBySender = borrowingRepo.findBySender(borrower);

        // Assertions
        assertEquals(2, requestsBySender.size());
        assertTrue(requestsBySender.stream().allMatch(r -> r.getSender().getId() == borrower.getId()));
    }

    @Test
    public void testFindAllRequestsByStatusAndSender() {
        // Create an owner
        Person person1 = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person1);

        GameOwner owner = new GameOwner();
        owner.setPerson(person1);
        gameOwnerRepo.save(owner);

        // Create a borrower
        Person person2 = new Person("bbbbbb@gmail.com", "bbbbb", "Patrick");
        personRepo.save(person2);

        Player borrower = new Player();
        borrower.setPerson(person2);
        playerRepo.save(borrower);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Create BorrowingRequests with different statuses for the same sender
        BorrowingRequest request1 = new BorrowingRequest(Date.valueOf("2023-10-01"), Date.valueOf("2023-10-10"),
                borrower, gameCopy);
        request1.setStatus(BorrowingRequestStatus.Rejected);
        borrowingRepo.save(request1);

        BorrowingRequest request2 = new BorrowingRequest(Date.valueOf("2023-11-01"), Date.valueOf("2023-11-10"),
                borrower, gameCopy);
        request2.setStatus(BorrowingRequestStatus.Accepted);
        borrowingRepo.save(request2);

        // Retrieve requests by status and sender
        List<BorrowingRequest> rejectedRequests = borrowingRepo.findAllRequestsByStatusAndSender(
                BorrowingRequestStatus.Rejected,
                borrower.getId());

        // Assertions
        assertEquals(1, rejectedRequests.size());
        assertEquals(request1.getId(), rejectedRequests.get(0).getId());
        assertEquals(BorrowingRequestStatus.Rejected, rejectedRequests.get(0).getStatus());
    }

    @Test
    public void testFindByGameCopy() {
        // Create an owner
        Person person1 = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person1);

        GameOwner owner = new GameOwner();
        owner.setPerson(person1);
        gameOwnerRepo.save(owner);

        // Create a borrower
        Person person2 = new Person("bbbbbb@gmail.com", "bbbbb", "Patrick");
        personRepo.save(person2);

        Player borrower = new Player();
        borrower.setPerson(person2);
        playerRepo.save(borrower);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Create BorrowingRequests for the same game copy
        BorrowingRequest request1 = new BorrowingRequest(Date.valueOf("2023-10-01"), Date.valueOf("2023-10-10"),
                borrower, gameCopy);
        borrowingRepo.save(request1);

        BorrowingRequest request2 = new BorrowingRequest(Date.valueOf("2023-11-01"), Date.valueOf("2023-11-10"),
                borrower, gameCopy);
        borrowingRepo.save(request2);

        // Retrieve requests by game copy
        List<BorrowingRequest> requestsByGameCopy = borrowingRepo.findByGameCopy(gameCopy);

        // Assertions
        assertEquals(2, requestsByGameCopy.size());
        assertTrue(requestsByGameCopy.stream().allMatch(r -> r.getGameCopy().getId() == gameCopy.getId()));
    }
}