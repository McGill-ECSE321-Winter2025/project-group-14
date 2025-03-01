package ca.mcgill.ecse321.gamenight.repo;

import ca.mcgill.ecse321.gamenight.model.*;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import org.junit.jupiter.api.*;
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

        private Person person1;
        private Person person2;
        private GameOwner owner;
        private Player borrower;
        private Game game;
        private GameCopy gameCopy;

        @BeforeEach
        public void setUp() {
                clearDatabase();

                person1 = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand", "firebase-uid-111");
                personRepo.save(person1);

                owner = new GameOwner(person1);
                gameOwnerRepo.save(owner);

                person2 = new Person("bbbbbb@gmail.com", "bbbbb", "Patrick", "firebase-uid222");
                personRepo.save(person2);

                borrower = new Player(person2);
                playerRepo.save(borrower);

                game = new Game("Batman", "A Batman game");
                gameRepo.save(game);

                gameCopy = new GameCopy("My copy of Batman", game, owner);
                gameCopyRepo.save(gameCopy);
        }

        @AfterEach
        public void clearDatabase() {
                borrowingRepo.deleteAll();
                gameCopyRepo.deleteAll();
                playerRepo.deleteAll();
                gameOwnerRepo.deleteAll();
                gameRepo.deleteAll();
                personRepo.deleteAll();
        }

        @Test
        public void testCreateAndReadBorrowingRequest() {
                Date startTime = Date.valueOf("2023-10-01");
                Date endTime = Date.valueOf("2023-10-10");
                BorrowingRequest request = new BorrowingRequest(startTime, endTime, borrower, gameCopy);
                borrowingRepo.save(request);

                BorrowingRequest retrievedRequest = borrowingRepo.findById(request.getId()).orElse(null);

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
                Date startTime = Date.valueOf("2023-10-01");
                Date endTime = Date.valueOf("2023-10-10");
                BorrowingRequest request = new BorrowingRequest(startTime, endTime, borrower, gameCopy);
                borrowingRepo.save(request);

                request.setStatus(BorrowingRequestStatus.Accepted);
                borrowingRepo.save(request);

                BorrowingRequest updatedRequest = borrowingRepo.findById(request.getId()).orElse(null);

                assertNotNull(updatedRequest);
                assertEquals(BorrowingRequestStatus.Accepted, updatedRequest.getStatus());
        }

        @Test
        public void testDeleteBorrowingRequest() {
                Date startTime = Date.valueOf("2023-10-01");
                Date endTime = Date.valueOf("2023-10-10");
                BorrowingRequest request = new BorrowingRequest(startTime, endTime, borrower, gameCopy);
                borrowingRepo.save(request);

                borrowingRepo.delete(request);

                BorrowingRequest deletedRequest = borrowingRepo.findById(request.getId()).orElse(null);

                assertNull(deletedRequest);
        }

        @Test
        public void testFindAllBorrowingRequests() {
                BorrowingRequest request1 = new BorrowingRequest(Date.valueOf("2023-10-01"), Date.valueOf("2023-10-10"),
                                borrower, gameCopy);
                BorrowingRequest request2 = new BorrowingRequest(Date.valueOf("2023-11-01"), Date.valueOf("2023-11-10"),
                                borrower, gameCopy);
                borrowingRepo.save(request1);
                borrowingRepo.save(request2);

                List<BorrowingRequest> requests = (List<BorrowingRequest>) borrowingRepo.findAll();

                assertEquals(2, requests.size());
        }

        @Test
        public void testFindAllRequestsByStatusAndGameOwner() {
                BorrowingRequest request1 = new BorrowingRequest(Date.valueOf("2023-10-01"), Date.valueOf("2023-10-10"),
                                borrower, gameCopy);
                request1.setStatus(BorrowingRequestStatus.Rejected);
                borrowingRepo.save(request1);

                BorrowingRequest request2 = new BorrowingRequest(Date.valueOf("2023-11-01"), Date.valueOf("2023-11-10"),
                                borrower, gameCopy);
                request2.setStatus(BorrowingRequestStatus.Accepted);
                borrowingRepo.save(request2);

                List<BorrowingRequest> pendingRequests = borrowingRepo
                                .findAllRequestsByStatusAndGameOwner(BorrowingRequestStatus.Rejected, owner.getId());

                assertEquals(1, pendingRequests.size());
                assertEquals(request1.getId(), pendingRequests.get(0).getId());
        }

        @Test
        public void testFindBySender() {
                BorrowingRequest request1 = new BorrowingRequest(Date.valueOf("2023-10-01"), Date.valueOf("2023-10-10"),
                                borrower, gameCopy);
                BorrowingRequest request2 = new BorrowingRequest(Date.valueOf("2023-11-01"), Date.valueOf("2023-11-10"),
                                borrower, gameCopy);
                borrowingRepo.save(request1);
                borrowingRepo.save(request2);

                List<BorrowingRequest> requestsBySender = borrowingRepo.findBySender(borrower);

                assertEquals(2, requestsBySender.size());
                assertTrue(requestsBySender.stream().allMatch(r -> r.getSender().getId() == borrower.getId()));
        }

        @Test
        public void testFindAllRequestsByStatusAndSender() {
                BorrowingRequest request1 = new BorrowingRequest(Date.valueOf("2023-10-01"), Date.valueOf("2023-10-10"),
                                borrower, gameCopy);
                request1.setStatus(BorrowingRequestStatus.Rejected);
                borrowingRepo.save(request1);

                BorrowingRequest request2 = new BorrowingRequest(Date.valueOf("2023-11-01"), Date.valueOf("2023-11-10"),
                                borrower, gameCopy);
                request2.setStatus(BorrowingRequestStatus.Accepted);
                borrowingRepo.save(request2);

                List<BorrowingRequest> rejectedRequests = borrowingRepo
                                .findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, borrower.getId());

                assertEquals(1, rejectedRequests.size());
                assertEquals(request1.getId(), rejectedRequests.get(0).getId());
                assertEquals(BorrowingRequestStatus.Rejected, rejectedRequests.get(0).getStatus());
        }

        @Test
        public void testFindByGameCopy() {
                BorrowingRequest request1 = new BorrowingRequest(Date.valueOf("2023-10-01"), Date.valueOf("2023-10-10"),
                                borrower, gameCopy);
                BorrowingRequest request2 = new BorrowingRequest(Date.valueOf("2023-11-01"), Date.valueOf("2023-11-10"),
                                borrower, gameCopy);
                borrowingRepo.save(request1);
                borrowingRepo.save(request2);

                List<BorrowingRequest> requestsByGameCopy = borrowingRepo.findByGameCopy(gameCopy);

                assertEquals(2, requestsByGameCopy.size());
                assertTrue(requestsByGameCopy.stream().allMatch(r -> r.getGameCopy().getId() == gameCopy.getId()));
        }
}