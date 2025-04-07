

package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.exception.EmailSendingFailedException;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
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
    private UserManagementService userManagementService;

    @InjectMocks
    private BorrowingManagementService borrowingManagementService;

    private Person senderPerson;
    private Person ownerPerson;
    private BorrowingRequest request;
    private Game game;
    private BorrowingRequest activeRequest;
    private BorrowingRequest inactiveRequest;

    @BeforeEach
    public void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
        game = new Game();
        game.setName("Uno");

        ownerPerson = new Person();
        ownerPerson.setName("Hamza");
        ownerPerson.setEmailAddress("hamza@example.com");

        senderPerson = new Person();
        senderPerson.setName("John");
        senderPerson.setEmailAddress("john@example.com");

        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson);

        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(game);
        gameCopy.setGameOwner(gameOwner);

        Player sender = new Player();
        sender.setPerson(senderPerson);

        request = new BorrowingRequest();
        request.setGameCopy(gameCopy);
        request.setSender(sender);
        request.setStatus(BorrowingRequestStatus.Delivered);

        activeRequest = new BorrowingRequest();
        activeRequest.setId(1);
        activeRequest.setStatus(BorrowingRequestStatus.Accepted);
        
        inactiveRequest = new BorrowingRequest();
        inactiveRequest.setId(2);
        inactiveRequest.setStatus(BorrowingRequestStatus.Accepted);
    }

    @Test
    public void testSendValidBorrowingRequest() {
        int gameCopyId = 10;
        int senderId = 5;
        LocalDate startTime = LocalDate.parse("2025-03-12");
        LocalDate endTime = LocalDate.parse("2025-03-15");

        // Game and Gameowner
        Game game = new Game();
        game.setName("uno");
        Person ownerPerson = new Person();
        ownerPerson.setName("Hamza");
        ownerPerson.setEmailAddress("hamza@example.com");
        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson);
        GameCopy gameCopy = new GameCopy();
        gameCopy.setId(gameCopyId);
        gameCopy.setGame(game);
        gameCopy.setGameOwner(gameOwner);

        // Sender
        Person senderPerson = new Person();
        senderPerson.setName("john");
        senderPerson.setEmailAddress("john@example.com");
        Player sender = new Player();
        sender.setId(senderId);
        sender.setPerson(senderPerson);

        BorrowingRequest request = new BorrowingRequest();
        request.setId(120);
        request.setStatus(BorrowingRequestStatus.Delivered);
        request.setGameCopy(gameCopy);
        request.setSender(sender);

        request.setStartTime(startTime);
        request.setEndTime(endTime);

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(gameCopy));
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class)))
                .thenAnswer(invocation -> {
                    BorrowingRequest savedRequest = invocation.getArgument(0);
                    savedRequest.setId(120);
                    return savedRequest;
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
        verify(emailService, times(1)).sendBorrowingRequestEmail(
                eq("hamza@example.com"),
                eq(senderPerson),
                eq("uno"));
    }

    @Test
    void testSendBorrowingRequest_EmailFailure() {
        int gameCopyId = 10;
        int senderId = 5;
        LocalDate startTime = LocalDate.parse("2025-03-12");
        LocalDate endTime = LocalDate.parse("2025-03-15");

        Game game = new Game();
        game.setName("Uno");
        Person ownerPerson = new Person();
        ownerPerson.setName("Owner");
        ownerPerson.setEmailAddress("owner@example.com");
        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson);
        GameCopy gameCopy = new GameCopy();
        gameCopy.setId(gameCopyId);
        gameCopy.setGame(game);
        gameCopy.setGameOwner(gameOwner);

        Person senderPerson = new Person();
        senderPerson.setName("John");
        senderPerson.setEmailAddress("john@example.com");
        Player sender = new Player();
        sender.setId(senderId);
        sender.setPerson(senderPerson);

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(gameCopy));
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        doThrow(new EmailSendingFailedException("Failed to send email"))
                .when(emailService).sendBorrowingRequestEmail(any(String.class), any(Person.class), any(String.class));
        Exception e = assertThrows(EmailSendingFailedException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });

        assertEquals("Failed to send email", e.getMessage());
    }

    @Test
    public void testSendBorrowingRequestInvalidGameCopy() {
        int gameCopyId = 10;
        int senderId = 5;
        LocalDate startTime = LocalDate.parse("2025-03-12");
        LocalDate endTime = LocalDate.parse("2025-03-15");

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.empty());

        Exception e = assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });

        String expectedMessage = "GameCopy with id " + gameCopyId + " not found.";
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    public void testSendBorrowingRequestInvalidSender() {
        int gameCopyId = 10;
        int senderId = 5;
        LocalDate startTime = LocalDate.parse("2025-03-12");
        LocalDate endTime = LocalDate.parse("2025-03-15");

        Game game = new Game();
        game.setName("uno");
        Person ownerPerson = new Person();
        ownerPerson.setName("Hamza");
        ownerPerson.setEmailAddress("owner@example.com");
        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson);
        GameCopy gameCopy = new GameCopy();
        gameCopy.setId(gameCopyId);
        gameCopy.setGame(game);
        gameCopy.setGameOwner(gameOwner);

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(gameCopy));
        when(playerRepository.findById(senderId)).thenReturn(Optional.empty());

        Exception e = assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });
        String expectedMessage = "Player not found with ID: " + senderId;
        assertEquals(expectedMessage, e.getMessage());
    }

    @Test
    void testRespondToBorrowingRequest_Accepted() {
        BorrowingRequest request = new BorrowingRequest();
        request.setId(1);
        request.setStatus(BorrowingRequestStatus.Delivered);

        Game game = new Game();
        game.setName("Uno");

        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(game);

        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson);
        gameCopy.setGameOwner(gameOwner);

        request.setGameCopy(gameCopy);

        Person senderPerson = new Person();
        int senderId = 5;
        senderPerson.setName("Hamza");
        senderPerson.setEmailAddress("hamza@gmail.com");
        Player sender = new Player();
        sender.setId(senderId);
        sender.setPerson(senderPerson);

        request.setSender(sender);

        when(borrowingRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(request,
                BorrowingRequestStatus.Accepted);

        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(any(BorrowingRequest.class));

    }

    @Test
    void testRespondToBorrowingRequest_Rejected() {
        BorrowingRequest request = new BorrowingRequest();
        request.setId(1);
        request.setStatus(BorrowingRequestStatus.Delivered);

        Game game = new Game();
        game.setName("Uno");
        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(game);

        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson);
        gameCopy.setGameOwner(gameOwner);
        request.setGameCopy(gameCopy);

        Person senderPerson = new Person();
        int senderId = 5;
        senderPerson.setName("Hamza");
        senderPerson.setEmailAddress("hamza@gmail.com");
        Player sender = new Player();
        sender.setId(senderId);
        sender.setPerson(senderPerson);

        request.setSender(sender);

        when(borrowingRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(request,
                BorrowingRequestStatus.Rejected);

        assertEquals(BorrowingRequestStatus.Rejected, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(any(BorrowingRequest.class));
    }

    @Test
    void testUpdateBorrowingRequestToAccepted() {
        BorrowingRequest request = new BorrowingRequest();
        request.setId(1);
        request.setStatus(BorrowingRequestStatus.Delivered);

        when(borrowingRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.updateBorrowingRequestStatus(request,
                BorrowingRequestStatus.Accepted);
        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(request); // used to make sure the code would be updating to
                                                                    // the database
    }

    @Test
    void testUpdateBorrowingRequestToRejected() {
        BorrowingRequest request = new BorrowingRequest();
        request.setId(1);
        request.setStatus(BorrowingRequestStatus.Delivered);

        when(borrowingRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.updateBorrowingRequestStatus(request,
                BorrowingRequestStatus.Rejected);
        assertEquals(BorrowingRequestStatus.Rejected, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(request); // used to make sure the code would be updating to
                                                                    // the database
    }

    @Test
    void testUpdateBorrowingRequestThatDoesNotExist() {
        BorrowingRequest request = new BorrowingRequest();
        when(borrowingRequestRepository.findById(383)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.updateBorrowingRequestStatus(request, BorrowingRequestStatus.Accepted);
        });
        verify(borrowingRequestRepository, never()).save(any(BorrowingRequest.class));
    }

    @Test
    void testFindDeliveredBorrowingRequestsForBorrowerValid() {
        int borrowerId = 1;
        BorrowingRequest request = new BorrowingRequest();
        request.setStatus(BorrowingRequestStatus.Delivered);
        BorrowingRequest request2 = new BorrowingRequest();
        request2.setStatus(BorrowingRequestStatus.Delivered);
        List<BorrowingRequest> deliveredList = Arrays.asList(request, request2);

        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId))
                .thenReturn(deliveredList);

        List<BorrowingRequest> result = borrowingManagementService
                .findDeliveredBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(req -> assertEquals(BorrowingRequestStatus.Delivered, req.getStatus()));

    }

    @Test
    void testFindDeliveredBorrowingRequestsForBorrowerInvalid() {
        int borrowerId = 999;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId))
                .thenReturn(Collections.emptyList());

        List<BorrowingRequest> result = borrowingManagementService
                .findDeliveredBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindRejectedBorrowingRequestsForBorrowerValid() {
        int borrowerId = 2;
        BorrowingRequest request = new BorrowingRequest();
        request.setStatus(BorrowingRequestStatus.Rejected);
        List<BorrowingRequest> rejectedList = Collections.singletonList(request);

        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, borrowerId))
                .thenReturn(rejectedList);

        List<BorrowingRequest> result = borrowingManagementService.findRejectedBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BorrowingRequestStatus.Rejected, result.get(0).getStatus());
    }

    @Test
    void testFindRejectedBorrowingRequestsForBorrowerInvalid() {
        int borrowerId = 999;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, borrowerId))
                .thenReturn(Collections.emptyList());

        List<BorrowingRequest> result = borrowingManagementService.findRejectedBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAcceptedBorrowingRequestsForBorrowerValid() {
        int borrowerId = 3;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, borrowerId))
                .thenReturn(Collections.emptyList());

        List<BorrowingRequest> result = borrowingManagementService.findAcceptedBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAcceptedBorrowingRequestsForBorrowerInvalid() {
        int borrowerId = 999;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, borrowerId))
                .thenReturn(Collections.emptyList());

        List<BorrowingRequest> result = borrowingManagementService.findAcceptedBorrowingRequestsForBorrower(borrowerId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindLendingHistoryValid() {
        int ownerId = 10;

        Person ownerPerson = new Person();
        ownerPerson.setEmailAddress("owner@example.com");
        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson);

        Game game = new Game();
        game.setName("Uno");

        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(game);
        gameCopy.setGameOwner(gameOwner);

        Person senderPerson = new Person();
        senderPerson.setEmailAddress("sender@example.com");
        Player sender = new Player();
        sender.setPerson(senderPerson);

        BorrowingRequest request = new BorrowingRequest();
        request.setStatus(BorrowingRequestStatus.Accepted);
        request.setGameCopy(gameCopy);
        request.setSender(sender);

        List<BorrowingRequest> history = Collections.singletonList(request);

        when(borrowingRequestRepository.findAllByGameCopy_GameOwner_Id(ownerId))
                .thenReturn(history);

        List<BorrowingRequest> result = borrowingManagementService.findLendingHistory(ownerId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BorrowingRequestStatus.Accepted, result.get(0).getStatus());
    }

    @Test
    void testLendingHistoryInvalidOwner() {
        GameCopy gameCopy = mock(GameCopy.class);
        when(borrowingRequestRepository.findByGameCopy(gameCopy)).thenReturn(Collections.emptyList());
        BorrowingRequest result = borrowingManagementService.findGameCopyLendingStatus(gameCopy);
        assertNull(result);
    }

    @Test
    public void testGetBorrowingRequestByIdValid() {
        int requestId = 100;
        BorrowingRequest request = new BorrowingRequest();
        request.setId(requestId);

        when(borrowingRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
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
            GameCopy gameCopy = gameCopyRepository.findById(nonExistentGameCopyId)
                    .orElseThrow(() -> new ObjectNotFoundException(String.valueOf(nonExistentGameCopyId)));
            borrowingManagementService.findGameCopyLendingStatus(gameCopy);
        });
        verify(gameCopyRepository).findById(nonExistentGameCopyId);
    }

    @Test
    public void testFindGameCopyLendingStatus_NoAcceptedRequest() {
        int gameCopyId = 1;
        GameCopy gameCopy = new GameCopy();
        gameCopy.setId(gameCopyId);

        List<BorrowingRequest> requests = new ArrayList<>();
        BorrowingRequest request = new BorrowingRequest();
        request.setStatus(BorrowingRequestStatus.Delivered);
        requests.add(request);

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(gameCopy));
        when(borrowingRequestRepository.findByGameCopy(gameCopy)).thenReturn(requests);
        BorrowingRequest result = borrowingManagementService.findGameCopyLendingStatus(gameCopy);
        assertNull(result, "Should return null when no accepted borrowing request exists");
        verify(borrowingRequestRepository).findByGameCopy(gameCopy);
    }

    @Test
    public void testFindGameCopyLendingStatus_AcceptedRequestFound() {
        int gameCopyId = 1;
        GameCopy gameCopy = new GameCopy();
        gameCopy.setId(gameCopyId);

        List<BorrowingRequest> requests = new ArrayList<>();
        BorrowingRequest acceptedRequest = new BorrowingRequest();
        acceptedRequest.setStatus(BorrowingRequestStatus.Accepted);
        requests.add(acceptedRequest);

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(gameCopy));
        when(borrowingRequestRepository.findByGameCopy(gameCopy)).thenReturn(requests);
        BorrowingRequest result = borrowingManagementService.findGameCopyLendingStatus(gameCopy);
        assertNotNull(result, "Should return the accepted borrowing request");
        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository).findByGameCopy(gameCopy);
    }

    @Test
    void testRespondToBorrowingRequest_OtherStatus() {
        BorrowingRequest request = new BorrowingRequest();
        request.setId(1);
        request.setStatus(BorrowingRequestStatus.Delivered);
        Game game = new Game();
        game.setName("Uno");

        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(game);

        GameOwner gameOwner = new GameOwner();
        Person ownerPerson = new Person();
        ownerPerson.setName("Hamza");
        ownerPerson.setEmailAddress("hamza@example.com");
        gameOwner.setPerson(ownerPerson);
        gameCopy.setGameOwner(gameOwner);

        Person senderPerson = new Person();
        senderPerson.setName("John");
        senderPerson.setEmailAddress("john@example.com");
        Player sender = new Player();
        sender.setId(5);
        sender.setPerson(senderPerson);

        request.setGameCopy(gameCopy);
        request.setSender(sender);

        when(borrowingRequestRepository.save(any(BorrowingRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(request,
                BorrowingRequestStatus.Delivered);

        assertEquals(BorrowingRequestStatus.Delivered, result.getStatus());
        verify(emailService, never()).sendRequestAcceptedEmail(anyString(), anyString(), anyString());
        verify(emailService, never()).sendRequestRejectedEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testGetPlayerById_PlayerExists() {
        int playerId = 1;
        Person person = new Person();
        person.setName("Test User");
        person.setEmailAddress("test@example.com");

        Player player = new Player();
        player.setPerson(person);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        Player result = userManagementService.getPlayerById(playerId);

        assertNotNull(result, "Expected a Player object, but got null.");
        assertEquals(person.getName(), result.getPerson().getName());
    }

    @Test
    public void testGetPlayerById_PlayerDoesNotExist() {
        int playerId = 999;

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class,
                () -> userManagementService.getPlayerById(playerId));

        assertEquals("Person not found with ID: " + playerId, exception.getMessage());
    }


    @Test
    public void testFindActiveBorrowingRequests() {
        // Mock current date
        LocalDate currentLocalDate = LocalDate.parse("2023-06-01");
        
        // Mock repository response
        when(borrowingRequestRepository.findActiveBorrowingRequestsForBorrower(
            anyInt(), 
            any(BorrowingRequestStatus.class), 
            any(LocalDate.class))
        ).thenReturn(Arrays.asList(activeRequest));

        // Call service method
        List<BorrowingRequest> results = borrowingManagementService.findActiveBorrowingRequestsForBorrower(1);

        // Verify results
        assertEquals(1, results.size());
        assertEquals(activeRequest.getId(), results.get(0).getId());
    }

}

