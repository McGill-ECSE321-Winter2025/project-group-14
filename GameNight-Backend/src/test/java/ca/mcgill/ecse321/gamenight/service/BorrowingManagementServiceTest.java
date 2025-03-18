package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.exceptions.EmailSendingFailedException;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.BorrowingRequestRepository;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;

@SpringBootTest
public class BorrowingManagementServiceTest {

    @Mock
    private BorrowingRequestRepository borrowingRequestRepository;

    @Mock
    private GameCopyRepository gameCopyRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BorrowingManagementService borrowingManagementService;

    // Default objects for tests
    private Person defaultSenderPerson;
    private Person defaultOwnerPerson;
    private Game defaultGame;
    private GameCopy defaultGameCopy;
    private Player defaultSender;
    private GameOwner defaultGameOwner;
    private BorrowingRequest defaultBorrowingRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        defaultGame = new Game();
        defaultGame.setName("Uno");
        defaultOwnerPerson = new Person();
        defaultOwnerPerson.setName("Hamza");
        defaultOwnerPerson.setEmailAddress("hamza@example.com");
        defaultSenderPerson = new Person();
        defaultSenderPerson.setName("John");
        defaultSenderPerson.setEmailAddress("john@example.com");
        defaultGameOwner = new GameOwner();
        defaultGameOwner.setPerson(defaultOwnerPerson);
        defaultGameCopy = new GameCopy();
        defaultGameCopy.setGame(defaultGame);
        defaultGameCopy.setGameOwner(defaultGameOwner);
        defaultSender = new Player();
        defaultSender.setPerson(defaultSenderPerson);
        defaultBorrowingRequest = new BorrowingRequest();
        defaultBorrowingRequest.setGameCopy(defaultGameCopy);
        defaultBorrowingRequest.setSender(defaultSender);
        defaultBorrowingRequest.setStatus(BorrowingRequestStatus.Delivered);
    }

    @Test
    public void sendValidBorrowingRequestTest() {
        int gameCopyId = 10;
        int senderId = 5;
        Date startTime = Date.valueOf("2025-03-12");
        Date endTime = Date.valueOf("2025-03-15");

        Game testGame = new Game();
        testGame.setName("uno");
        Person testOwner = new Person();
        testOwner.setName("Hamza");
        testOwner.setEmailAddress("hamza@example.com");
        GameOwner testGameOwner = new GameOwner();
        testGameOwner.setPerson(testOwner);
        GameCopy testGameCopy = new GameCopy();
        testGameCopy.setId(gameCopyId);
        testGameCopy.setGame(testGame);
        testGameCopy.setGameOwner(testGameOwner);

        Person testSender = new Person();
        testSender.setName("john");
        testSender.setEmailAddress("john@example.com");
        Player testPlayer = new Player();
        testPlayer.setId(senderId);
        testPlayer.setPerson(testSender);

        BorrowingRequest testRequest = new BorrowingRequest();
        testRequest.setId(120);
        testRequest.setStatus(BorrowingRequestStatus.Delivered);
        testRequest.setGameCopy(testGameCopy);
        testRequest.setSender(testPlayer);
        testRequest.setStartTime(startTime);
        testRequest.setEndTime(endTime);

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(testGameCopy));
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(testPlayer));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class)))
                .thenAnswer(invocation -> {
                    BorrowingRequest saved = invocation.getArgument(0);
                    saved.setId(120);
                    return saved;
                });

        BorrowingRequest result = borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime,
                endTime);

        assertNotNull(result);
        assertEquals(BorrowingRequestStatus.Delivered, result.getStatus());
        assertEquals("uno", result.getGameCopy().getGame().getName());
        assertEquals("john", result.getSender().getPerson().getName());
        assertNotNull(result.getSendTime());

        long currentTime = System.currentTimeMillis();
        long sendTimeMillis = result.getSendTime().getTime();
        assertTrue(Math.abs(currentTime - sendTimeMillis) < 2000, "Send time must be within 2 sec of current time");

        verify(emailService, times(1)).sendBorrowingRequestEmail(eq("hamza@example.com"), eq(testSender), eq("uno"));
    }

    @Test
    void testSendBorrowingRequest_EmailFailure() {
        int gameCopyId = 10;
        int senderId = 5;
        Date startTime = Date.valueOf("2025-03-12");
        Date endTime = Date.valueOf("2025-03-15");

        Game testGame = new Game();
        testGame.setName("Uno");
        Person testOwner = new Person();
        testOwner.setName("Owner");
        testOwner.setEmailAddress("owner@example.com");
        GameOwner testGameOwner = new GameOwner();
        testGameOwner.setPerson(testOwner);
        GameCopy testGameCopy = new GameCopy();
        testGameCopy.setId(gameCopyId);
        testGameCopy.setGame(testGame);
        testGameCopy.setGameOwner(testGameOwner);

        Person testSender = new Person();
        testSender.setName("John");
        testSender.setEmailAddress("john@example.com");
        Player testPlayer = new Player();
        testPlayer.setId(senderId);
        testPlayer.setPerson(testSender);

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(testGameCopy));
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(testPlayer));
        doThrow(new EmailSendingFailedException("Failed to send email"))
                .when(emailService).sendBorrowingRequestEmail(any(String.class), any(Person.class), any(String.class));

        Exception e = assertThrows(EmailSendingFailedException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });
        assertEquals("Failed to send email", e.getMessage());
    }

    @Test
    public void sendBorrowingRequestInvalidGameCopyTest() {
        int gameCopyId = 10;
        int senderId = 5;
        Date startTime = Date.valueOf("2025-03-12");
        Date endTime = Date.valueOf("2025-03-15");

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.empty());

        Exception e = assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });

        String expectedMessage = "GameCopy with id " + gameCopyId + " not found.";
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    public void sendBorrowingRequestInvalidSenderTest() {
        int gameCopyId = 10;
        int senderId = 5;
        Date startTime = Date.valueOf("2025-03-12");
        Date endTime = Date.valueOf("2025-03-15");

        Game testGame = new Game();
        testGame.setName("uno");
        Person testOwner = new Person();
        testOwner.setName("Hamza");
        testOwner.setEmailAddress("owner@example.com");
        GameOwner testGameOwner = new GameOwner();
        testGameOwner.setPerson(testOwner);
        GameCopy testGameCopy = new GameCopy();
        testGameCopy.setId(gameCopyId);
        testGameCopy.setGame(testGame);
        testGameCopy.setGameOwner(testGameOwner);

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(testGameCopy));
        when(playerRepository.findById(senderId)).thenReturn(Optional.empty());

        Exception e = assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });
        String expectedMessage = "Player not found with ID: " + senderId;
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    void testRespondToBorrowingRequest_Accepted() {
        BorrowingRequest testRequest = new BorrowingRequest();
        testRequest.setId(1);
        testRequest.setStatus(BorrowingRequestStatus.Delivered);

        Game testGame = new Game();
        testGame.setName("Uno");
        GameCopy testGameCopy = new GameCopy();
        testGameCopy.setGame(testGame);
        GameOwner testGameOwner = new GameOwner();
        testGameOwner.setPerson(defaultOwnerPerson);
        testGameCopy.setGameOwner(testGameOwner);
        testRequest.setGameCopy(testGameCopy);

        Player testPlayer = new Player();
        testPlayer.setId(5);
        testPlayer.setPerson(defaultSenderPerson);
        testRequest.setSender(testPlayer);

        when(borrowingRequestRepository.findById(1)).thenReturn(Optional.of(testRequest));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class)))
                .thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(testRequest,
                BorrowingRequestStatus.Accepted);
        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(any(BorrowingRequest.class));
    }

    @Test
    void testRespondToBorrowingRequest_Rejected() {
        BorrowingRequest testRequest = new BorrowingRequest();
        testRequest.setId(1);
        testRequest.setStatus(BorrowingRequestStatus.Delivered);

        Game testGame = new Game();
        testGame.setName("Uno");
        GameCopy testGameCopy = new GameCopy();
        testGameCopy.setGame(testGame);
        GameOwner testGameOwner = new GameOwner();
        testGameOwner.setPerson(defaultOwnerPerson);
        testGameCopy.setGameOwner(testGameOwner);
        testRequest.setGameCopy(testGameCopy);

        Player testPlayer = new Player();
        testPlayer.setId(5);
        testPlayer.setPerson(defaultSenderPerson);
        testRequest.setSender(testPlayer);

        when(borrowingRequestRepository.findById(1)).thenReturn(Optional.of(testRequest));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class)))
                .thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(testRequest,
                BorrowingRequestStatus.Rejected);
        assertEquals(BorrowingRequestStatus.Rejected, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(any(BorrowingRequest.class));
    }

    @Test
    void testUpdateBorrowingRequestToAccepted() {
        BorrowingRequest testRequest = new BorrowingRequest();
        testRequest.setId(1);
        testRequest.setStatus(BorrowingRequestStatus.Delivered);

        when(borrowingRequestRepository.findById(1)).thenReturn(Optional.of(testRequest));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class)))
                .thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.updateBorrowingRequestStatus(testRequest,
                BorrowingRequestStatus.Accepted);
        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(testRequest);
    }

    @Test
    void testUpdateBorrowingRequestToRejected() {
        BorrowingRequest testRequest = new BorrowingRequest();
        testRequest.setId(1);
        testRequest.setStatus(BorrowingRequestStatus.Delivered);

        when(borrowingRequestRepository.findById(1)).thenReturn(Optional.of(testRequest));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class)))
                .thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.updateBorrowingRequestStatus(testRequest,
                BorrowingRequestStatus.Rejected);
        assertEquals(BorrowingRequestStatus.Rejected, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(testRequest);
    }

    @Test
    void testUpdateBorrowingRequestThatDoesNotExist() {
        BorrowingRequest testRequest = new BorrowingRequest();
        when(borrowingRequestRepository.findById(383)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.updateBorrowingRequestStatus(testRequest, BorrowingRequestStatus.Accepted);
        });
        verify(borrowingRequestRepository, never()).save(any(BorrowingRequest.class));
    }

    @Test
    void testfindDeliveredBorrowingRequestsForBorrowerValid() {
        int borrowerId = 1;
        BorrowingRequest req1 = new BorrowingRequest();
        req1.setStatus(BorrowingRequestStatus.Delivered);
        BorrowingRequest req2 = new BorrowingRequest();
        req2.setStatus(BorrowingRequestStatus.Delivered);
        List<BorrowingRequest> deliveredList = Arrays.asList(req1, req2);

        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId))
                .thenReturn(deliveredList);

        List<BorrowingRequest> result = borrowingManagementService
                .findDeliveredBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(req -> assertEquals(BorrowingRequestStatus.Delivered, req.getStatus()));
    }

    @Test
    void testfindDeliveredBorrowingRequestsForBorrowerInvalid() {
        int borrowerId = 999;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId))
                .thenReturn(Collections.emptyList());
        List<BorrowingRequest> result = borrowingManagementService
                .findDeliveredBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testfindRejectedBorrowingRequestsForBorrowerValid() {
        int borrowerId = 2;
        BorrowingRequest req = new BorrowingRequest();
        req.setStatus(BorrowingRequestStatus.Rejected);
        List<BorrowingRequest> rejectedList = Collections.singletonList(req);

        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, borrowerId))
                .thenReturn(rejectedList);

        List<BorrowingRequest> result = borrowingManagementService.findRejectedBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BorrowingRequestStatus.Rejected, result.get(0).getStatus());
    }

    @Test
    void testfindRejectedBorrowingRequestsForBorrowerInvalid() {
        int borrowerId = 999;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, borrowerId))
                .thenReturn(Collections.emptyList());
        List<BorrowingRequest> result = borrowingManagementService.findRejectedBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testfindAcceptedBorrowingRequestsForBorrowerValid() {
        int borrowerId = 3;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, borrowerId))
                .thenReturn(Collections.emptyList());
        List<BorrowingRequest> result = borrowingManagementService.findAcceptedBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testfindAcceptedBorrowingRequestsForBorrowerInvalid() {
        int borrowerId = 999;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, borrowerId))
                .thenReturn(Collections.emptyList());
        List<BorrowingRequest> result = borrowingManagementService.findAcceptedBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testfindLendingHistoryValid() {
        int ownerId = 10;
        BorrowingRequest req = new BorrowingRequest();
        req.setStatus(BorrowingRequestStatus.Accepted);
        List<BorrowingRequest> history = Collections.singletonList(req);

        when(borrowingRequestRepository.findAllRequestsByStatusAndGameOwner(BorrowingRequestStatus.Accepted, ownerId))
                .thenReturn(history);
        List<BorrowingRequest> result = borrowingManagementService.findLendingHistory(ownerId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BorrowingRequestStatus.Accepted, result.get(0).getStatus());
    }

    @Test
    void testLendingHistoryInvalidOwner() {
        GameCopy testGameCopy = mock(GameCopy.class);
        when(borrowingRequestRepository.findByGameCopy(testGameCopy)).thenReturn(Collections.emptyList());
        BorrowingRequest result = borrowingManagementService.findGameCopyLendingStatus(testGameCopy);
        assertNull(result);
    }

    @Test
    public void testGetBorrowingRequestByIdValid() {
        int requestId = 100;
        BorrowingRequest req = new BorrowingRequest();
        req.setId(requestId);

        when(borrowingRequestRepository.findById(requestId)).thenReturn(Optional.of(req));
        BorrowingRequest result = borrowingManagementService.getBorrowingRequestById(requestId);
        assertNotNull(result);
        assertEquals(requestId, result.getId());
    }

    @Test
    public void testGetBorrowingRequestByIdInvalid() {
        int requestId = 100;
        when(borrowingRequestRepository.findById(requestId)).thenReturn(Optional.empty());
        Exception e = assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.getBorrowingRequestById(requestId);
        });
        String expectedMessage = "Borrowing request not found with ID: " + requestId;
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    public void testFindGameCopyLendingStatus_GameCopyNotFound() {
        int nonExistentGameCopyId = 999;
        when(gameCopyRepository.findById(nonExistentGameCopyId)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            GameCopy gc = gameCopyRepository.findById(nonExistentGameCopyId)
                    .orElseThrow(() -> new ObjectNotFoundException(String.valueOf(nonExistentGameCopyId)));
            borrowingManagementService.findGameCopyLendingStatus(gc);
        });
        verify(gameCopyRepository).findById(nonExistentGameCopyId);
    }

    @Test
    public void testFindGameCopyLendingStatus_NoAcceptedRequest() {
        int gameCopyId = 1;
        GameCopy testGameCopy = new GameCopy();
        testGameCopy.setId(gameCopyId);

        List<BorrowingRequest> requests = new ArrayList<>();
        BorrowingRequest req = new BorrowingRequest();
        req.setStatus(BorrowingRequestStatus.Delivered);
        requests.add(req);

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(testGameCopy));
        when(borrowingRequestRepository.findByGameCopy(testGameCopy)).thenReturn(requests);
        BorrowingRequest result = borrowingManagementService.findGameCopyLendingStatus(testGameCopy);
        assertNull(result, "Should return null when no accepted borrowing request exists");
        verify(borrowingRequestRepository).findByGameCopy(testGameCopy);
    }

    @Test
    public void testFindGameCopyLendingStatus_AcceptedRequestFound() {
        int gameCopyId = 1;
        GameCopy testGameCopy = new GameCopy();
        testGameCopy.setId(gameCopyId);

        List<BorrowingRequest> requests = new ArrayList<>();
        BorrowingRequest acceptedRequest = new BorrowingRequest();
        acceptedRequest.setStatus(BorrowingRequestStatus.Accepted);
        requests.add(acceptedRequest);

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(testGameCopy));
        when(borrowingRequestRepository.findByGameCopy(testGameCopy)).thenReturn(requests);
        BorrowingRequest result = borrowingManagementService.findGameCopyLendingStatus(testGameCopy);
        assertNotNull(result, "Should return the accepted borrowing request");
        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository).findByGameCopy(testGameCopy);
    }

    @Test
    void testRespondToBorrowingRequest_OtherStatus() {
        BorrowingRequest testRequest = new BorrowingRequest();
        testRequest.setId(1);
        testRequest.setStatus(BorrowingRequestStatus.Delivered);

        Game testGame = new Game();
        testGame.setName("Uno");
        GameCopy testGameCopy = new GameCopy();
        testGameCopy.setGame(testGame);
        GameOwner testGameOwner = new GameOwner();
        Person testOwner = new Person();
        testOwner.setName("Hamza");
        testOwner.setEmailAddress("hamza@example.com");
        testGameOwner.setPerson(testOwner);
        testGameCopy.setGameOwner(testGameOwner);
        testRequest.setGameCopy(testGameCopy);

        Person testSender = new Person();
        testSender.setName("John");
        testSender.setEmailAddress("john@example.com");
        Player testPlayer = new Player();
        testPlayer.setId(5);
        testPlayer.setPerson(testSender);
        testRequest.setSender(testPlayer);

        when(borrowingRequestRepository.save(any(BorrowingRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(testRequest,
                BorrowingRequestStatus.Delivered);
        assertEquals(BorrowingRequestStatus.Delivered, result.getStatus());
        verify(emailService, never()).sendRequestAcceptedEmail(anyString(), anyString(), anyString());
        verify(emailService, never()).sendRequestRejectedEmail(anyString(), anyString(), anyString());
    }
}

