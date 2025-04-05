package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.junit.jupiter.MockitoExtension; // Recommended for mockito initialization
import org.junit.jupiter.api.extension.ExtendWith; // Recommended for mockito initialization


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
@ExtendWith(MockitoExtension.class) // Use this for cleaner mock initialization instead of @BeforeEach with openMocks
public class BorrowingManagementServiceTest {

    @Mock
    private BorrowingRequestRepository borrowingRequestRepository;

    @Mock
    private GameCopyRepository gameCopyRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private EmailService emailService;

    // InjectMocks should be on the class under test
    @InjectMocks
    private BorrowingManagementService borrowingManagementService;

    // You might need UserManagementService if getPlayerById is used elsewhere,
    // but let's keep it separate if BorrowingManagementService doesn't directly use it.
    // If BorrowingManagementService calls UserManagementService, mock that too.
    // @InjectMocks // Can only have one @InjectMocks target typically
    // private UserManagementService userManagementService; // Let's remove this for now unless needed

    private Person senderPerson;
    private Person ownerPerson;
    private BorrowingRequest request;
    private Game game;

    // @BeforeEach is fine if you prefer it over @ExtendWith
    // @BeforeEach
    // public void setUp() {
    //     // org.mockito.MockitoAnnotations.openMocks(this); // Not needed with @ExtendWith
    //     game = new Game();
    //     game.setName("Uno");
    // 
    //     ownerPerson = new Person();
    //     ownerPerson.setName("Hamza");
    //     ownerPerson.setEmailAddress("hamza@example.com");
    // 
    //     senderPerson = new Person();
    //     senderPerson.setName("John");
    //     senderPerson.setEmailAddress("john@example.com");
    // 
    //     GameOwner gameOwner = new GameOwner();
    //     gameOwner.setPerson(ownerPerson);
    // 
    //     GameCopy gameCopy = new GameCopy();
    //     gameCopy.setGame(game);
    //     gameCopy.setGameOwner(gameOwner);
    // 
    //     Player sender = new Player();
    //     sender.setPerson(senderPerson);
    // 
    //     request = new BorrowingRequest();
    //     request.setGameCopy(gameCopy);
    //     request.setSender(sender);
    //     request.setStatus(BorrowingRequestStatus.Delivered); // Default status for setup
    // }

    @Test
    public void testSendValidBorrowingRequest() {
        int gameCopyId = 10;
        int senderId = 5;
        // Use java.sql.Date if your model uses it, otherwise java.util.Date
        Date startTime = Date.valueOf("2025-03-12"); 
        Date endTime = Date.valueOf("2025-03-15");

        // --- Setup Entities ---
        Game game = new Game();
        game.setName("uno");
        Person ownerPerson = new Person();
        ownerPerson.setName("Hamza");
        ownerPerson.setEmailAddress("hamza@example.com");
        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson); // Link owner person
        GameCopy gameCopy = new GameCopy();
        gameCopy.setId(gameCopyId);
        gameCopy.setGame(game);
        gameCopy.setGameOwner(gameOwner); // Link game owner

        Person senderPerson = new Person();
        senderPerson.setName("john");
        senderPerson.setEmailAddress("john@example.com");
        Player sender = new Player();
        sender.setId(senderId);
        sender.setPerson(senderPerson); // Link sender person

        // --- Mock Repository Calls ---
        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(gameCopy));
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        // Mock the save operation to return the saved entity with an ID
        when(borrowingRequestRepository.save(any(BorrowingRequest.class)))
                .thenAnswer(invocation -> {
                    BorrowingRequest savedRequest = invocation.getArgument(0);
                    // Simulate ID generation if needed, or just return the argument
                    savedRequest.setId(120); // Example ID
                    return savedRequest;
                });

        // --- Call Service Method ---
        BorrowingRequest result = borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);

        // --- Assertions ---
        assertNotNull(result);
        assertEquals(BorrowingRequestStatus.Delivered, result.getStatus()); // Should be Delivered initially
        assertEquals("uno", result.getGameCopy().getGame().getName());
        assertEquals("john", result.getSender().getPerson().getName());
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());
        assertEquals(120, result.getId());

        assertNotNull(result.getSendTime());
        long currentTime = System.currentTimeMillis();
        long sendTimeMillis = result.getSendTime().getTime();
        assertTrue(Math.abs(currentTime - sendTimeMillis) < 5000, "Send time must be within 5 sec of current time"); // Increased tolerance slightly

        // --- Verify Interactions ---
        verify(borrowingRequestRepository, times(1)).save(any(BorrowingRequest.class));
        // Verify email sending
        verify(emailService, times(1)).sendBorrowingRequestEmail(
                eq("hamza@example.com"),
                eq(senderPerson), // Pass the Person object
                eq("uno"));
    }

    @Test
    void testSendBorrowingRequest_EmailFailure() {
        int gameCopyId = 10;
        int senderId = 5;
        Date startTime = Date.valueOf("2025-03-12");
        Date endTime = Date.valueOf("2025-03-15");

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
        // Simulate email sending failure
        doThrow(new EmailSendingFailedException("Failed to send email"))
                .when(emailService).sendBorrowingRequestEmail(anyString(), any(Person.class), anyString());

        // --- Assert Exception ---
        EmailSendingFailedException e = assertThrows(EmailSendingFailedException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });

        assertEquals("Failed to send email", e.getMessage());
        // Verify save was NOT called because email failed *after* potential save in service
        // (Depends on service implementation order - checking service code, save happens first)
        // So, save *should* have been called once before the exception.
        // Let's refine the verification based on the service code:
        verify(borrowingRequestRepository, times(1)).save(any(BorrowingRequest.class)); 
        verify(emailService, times(1)).sendBorrowingRequestEmail(anyString(), any(Person.class), anyString());
    }

    @Test
    public void testSendBorrowingRequestInvalidGameCopy() {
        int gameCopyId = 10;
        int senderId = 5;
        Date startTime = Date.valueOf("2025-03-12");
        Date endTime = Date.valueOf("2025-03-15");

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.empty());
        // Mock player repo just in case service checks it later (though it should fail earlier)
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(new Player())); 

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });

        String expectedMessage = "GameCopy with id " + gameCopyId + " not found.";
        assertEquals(expectedMessage, e.getMessage());
        verify(borrowingRequestRepository, never()).save(any(BorrowingRequest.class));
        verify(emailService, never()).sendBorrowingRequestEmail(anyString(), any(Person.class), anyString());
    }

    @Test
    public void testSendBorrowingRequestInvalidSender() {
        int gameCopyId = 10;
        int senderId = 5;
        Date startTime = Date.valueOf("2025-03-12");
        Date endTime = Date.valueOf("2025-03-15");

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
        when(playerRepository.findById(senderId)).thenReturn(Optional.empty()); // Player not found

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });
        String expectedMessage = "Player not found with ID: " + senderId;
        assertEquals(expectedMessage, e.getMessage());
        verify(borrowingRequestRepository, never()).save(any(BorrowingRequest.class));
         verify(emailService, never()).sendBorrowingRequestEmail(anyString(), any(Person.class), anyString());
    }

    @Test
    void testRespondToBorrowingRequest_Accepted() {
        int requestId = 1;
        BorrowingRequest request = new BorrowingRequest();
        request.setId(requestId);
        request.setStatus(BorrowingRequestStatus.Delivered); // Initial status

        Game game = new Game();
        game.setName("Uno");
        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(game);

        Person ownerPerson = new Person(); // Create owner Person
        ownerPerson.setName("Owner");
        ownerPerson.setEmailAddress("owner@example.com");
        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson); // Link owner Person
        gameCopy.setGameOwner(gameOwner); // Link GameOwner
        request.setGameCopy(gameCopy);

        Person senderPerson = new Person();
        senderPerson.setName("Sender");
        senderPerson.setEmailAddress("sender@example.com");
        Player sender = new Player();
        sender.setId(5); // Example ID
        sender.setPerson(senderPerson); // Link sender Person
        request.setSender(sender);

        // No need to mock findById if request object is passed directly
        // when(borrowingRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        // --- Call Service Method ---
        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(request, BorrowingRequestStatus.Accepted);

        // --- Assertions ---
        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(request); // Verify save was called with the request
        verify(emailService, times(1)).sendRequestAcceptedEmail(
            eq("sender@example.com"), 
            eq("Owner"), 
            eq("Uno")
        );
        verify(emailService, never()).sendRequestRejectedEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testRespondToBorrowingRequest_Rejected() {
       int requestId = 1;
        BorrowingRequest request = new BorrowingRequest();
        request.setId(requestId);
        request.setStatus(BorrowingRequestStatus.Delivered); // Initial status

        Game game = new Game();
        game.setName("Uno");
        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(game);

        Person ownerPerson = new Person(); // Create owner Person
        ownerPerson.setName("Owner");
        ownerPerson.setEmailAddress("owner@example.com");
        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson); // Link owner Person
        gameCopy.setGameOwner(gameOwner); // Link GameOwner
        request.setGameCopy(gameCopy);

        Person senderPerson = new Person();
        senderPerson.setName("Sender");
        senderPerson.setEmailAddress("sender@example.com");
        Player sender = new Player();
        sender.setId(5); // Example ID
        sender.setPerson(senderPerson); // Link sender Person
        request.setSender(sender);


        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(request, BorrowingRequestStatus.Rejected);

        assertEquals(BorrowingRequestStatus.Rejected, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(request);
        verify(emailService, times(1)).sendRequestRejectedEmail(
            eq("sender@example.com"), 
            eq("Owner"), 
            eq("Uno")
        );
         verify(emailService, never()).sendRequestAcceptedEmail(anyString(), anyString(), anyString());
    }

     @Test
    void testRespondToBorrowingRequest_OtherStatus() {
        // Similar setup as Accepted/Rejected tests
        int requestId = 1;
        BorrowingRequest request = new BorrowingRequest();
        request.setId(requestId);
        request.setStatus(BorrowingRequestStatus.Accepted); // Initial status is now Accepted

        Game game = new Game(); game.setName("Uno");
        GameCopy gameCopy = new GameCopy(); gameCopy.setGame(game);
        Person ownerPerson = new Person(); ownerPerson.setName("Owner"); ownerPerson.setEmailAddress("owner@example.com");
        GameOwner gameOwner = new GameOwner(); gameOwner.setPerson(ownerPerson);
        gameCopy.setGameOwner(gameOwner);
        request.setGameCopy(gameCopy);
        Person senderPerson = new Person(); senderPerson.setName("Sender"); senderPerson.setEmailAddress("sender@example.com");
        Player sender = new Player(); sender.setId(5); sender.setPerson(senderPerson);
        request.setSender(sender);

        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        // Try changing status to something other than Accepted/Rejected (e.g., back to Delivered)
        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(request, BorrowingRequestStatus.Delivered);

        assertEquals(BorrowingRequestStatus.Delivered, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(request);
        // Verify NO email was sent for this status change
        verify(emailService, never()).sendRequestAcceptedEmail(anyString(), anyString(), anyString());
        verify(emailService, never()).sendRequestRejectedEmail(anyString(), anyString(), anyString());
    }


    // --- Tests for updateBorrowingRequestStatus ---

    @Test
    void testUpdateBorrowingRequestStatus_ToAccepted() {
        int requestId = 1;
        BorrowingRequest request = new BorrowingRequest();
        request.setId(requestId);
        request.setStatus(BorrowingRequestStatus.Delivered);

        when(borrowingRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.updateBorrowingRequestStatus(request, BorrowingRequestStatus.Accepted);
        
        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        // Verify findById was called to fetch the existing request
        verify(borrowingRequestRepository, times(1)).findById(requestId); 
        verify(borrowingRequestRepository, times(1)).save(request); 
    }

    @Test
    void testUpdateBorrowingRequestStatus_ToRejected() {
         int requestId = 1;
        BorrowingRequest request = new BorrowingRequest();
        request.setId(requestId);
        request.setStatus(BorrowingRequestStatus.Delivered);

        when(borrowingRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.updateBorrowingRequestStatus(request, BorrowingRequestStatus.Rejected);

        assertEquals(BorrowingRequestStatus.Rejected, result.getStatus());
        verify(borrowingRequestRepository, times(1)).findById(requestId);
        verify(borrowingRequestRepository, times(1)).save(request);
    }

    @Test
    void testUpdateBorrowingRequestStatus_NotFound() {
        int requestId = 383;
        BorrowingRequest request = new BorrowingRequest(); // Create a request object to pass
        request.setId(requestId);

        when(borrowingRequestRepository.findById(requestId)).thenReturn(Optional.empty()); // Simulate not found

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> {
            // Pass the request object, the service method uses its ID internally
            borrowingManagementService.updateBorrowingRequestStatus(request, BorrowingRequestStatus.Accepted); 
        });

        assertEquals("Borrowing request not found", e.getMessage()); // Check message from service
        verify(borrowingRequestRepository, times(1)).findById(requestId); // Verify find was attempted
        verify(borrowingRequestRepository, never()).save(any(BorrowingRequest.class)); // Verify save was never called
    }


    // --- Tests for find* methods ---

    @Test
    void testFindDeliveredBorrowingRequestsForBorrowerValid() {
        int borrowerId = 1;
        // Create some mock requests
        BorrowingRequest req1 = mock(BorrowingRequest.class);
        when(req1.getStatus()).thenReturn(BorrowingRequestStatus.Delivered);
        BorrowingRequest req2 = mock(BorrowingRequest.class);
        when(req2.getStatus()).thenReturn(BorrowingRequestStatus.Delivered);
        List<BorrowingRequest> deliveredList = Arrays.asList(req1, req2);

        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId))
                .thenReturn(deliveredList);

        List<BorrowingRequest> result = borrowingManagementService.findDeliveredBorrowingRequestsForBorrower(borrowerId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        // Optional: verify status if using real objects, unnecessary with mocks returning the list directly
        // result.forEach(req -> assertEquals(BorrowingRequestStatus.Delivered, req.getStatus())); 
        verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId);
    }

    @Test
    void testFindDeliveredBorrowingRequestsForBorrowerInvalid() {
        int borrowerId = 999;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId))
                .thenReturn(Collections.emptyList()); // Return empty list

        List<BorrowingRequest> result = borrowingManagementService.findDeliveredBorrowingRequestsForBorrower(borrowerId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId);
    }

    @Test
    void testFindRejectedBorrowingRequestsForBorrowerValid() {
        int borrowerId = 2;
        BorrowingRequest req1 = mock(BorrowingRequest.class);
        when(req1.getStatus()).thenReturn(BorrowingRequestStatus.Rejected);
        List<BorrowingRequest> rejectedList = Collections.singletonList(req1);

        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, borrowerId))
                .thenReturn(rejectedList);

        List<BorrowingRequest> result = borrowingManagementService.findRejectedBorrowingRequestsForBorrower(borrowerId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        // assertEquals(BorrowingRequestStatus.Rejected, result.get(0).getStatus()); // Not needed with mock
         verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, borrowerId);
    }

    @Test
    void testFindRejectedBorrowingRequestsForBorrowerInvalid() {
        int borrowerId = 999;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, borrowerId))
                .thenReturn(Collections.emptyList());

        List<BorrowingRequest> result = borrowingManagementService.findRejectedBorrowingRequestsForBorrower(borrowerId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
         verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, borrowerId);
    }

    @Test
    void testFindAcceptedBorrowingRequestsForBorrowerValid() {
         int borrowerId = 3;
         BorrowingRequest req1 = mock(BorrowingRequest.class);
         when(req1.getStatus()).thenReturn(BorrowingRequestStatus.Accepted);
         List<BorrowingRequest> acceptedList = Collections.singletonList(req1);

        // Mock the repository call for Accepted status
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, borrowerId))
                .thenReturn(acceptedList); 

        List<BorrowingRequest> result = borrowingManagementService.findAcceptedBorrowingRequestsForBorrower(borrowerId);
        
        assertNotNull(result);
        assertEquals(1, result.size()); // Expect 1 based on mock
        verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, borrowerId);
    }

    @Test
    void testFindAcceptedBorrowingRequestsForBorrowerInvalid() {
        int borrowerId = 999;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, borrowerId))
                .thenReturn(Collections.emptyList());

        List<BorrowingRequest> result = borrowingManagementService.findAcceptedBorrowingRequestsForBorrower(borrowerId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, borrowerId);

    }

    // --- Test for findLendingHistory (The one that failed previously) ---
    @Test
    void testFindLendingHistoryValid() {
        int ownerId = 10; 

        // --- Setup Mocks ---
        // We don't need full entity setup if we just mock the repository result
        BorrowingRequest acceptedRequest = mock(BorrowingRequest.class); 
        // Mock necessary methods if accessed in assertions later
        // GameCopy mockCopy = mock(GameCopy.class);
        // GameOwner mockOwner = mock(GameOwner.class);
        // when(acceptedRequest.getGameCopy()).thenReturn(mockCopy);
        // when(mockCopy.getOwner()).thenReturn(mockOwner);
        // when(mockOwner.getId()).thenReturn(ownerId); 
        when(acceptedRequest.getStatus()).thenReturn(BorrowingRequestStatus.Accepted); // Ensure status is correct

        List<BorrowingRequest> history = Collections.singletonList(acceptedRequest);

        // --- Mock the CORRECT repository method used in the service ---
        when(borrowingRequestRepository.findAllRequestsByStatusAndGameOwner(
                eq(BorrowingRequestStatus.Accepted), // Status should be Accepted
                eq(ownerId))) 
                .thenReturn(history);

        // --- Call the service method ---
        List<BorrowingRequest> result = borrowingManagementService.findLendingHistory(ownerId);

        // --- Assertions ---
        assertNotNull(result);
        assertEquals(1, result.size(), "Expected one borrowing request in history"); 
        assertEquals(BorrowingRequestStatus.Accepted, result.get(0).getStatus()); // Verify status of returned item
        
        // Verify the correct repository method was called
        verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndGameOwner(BorrowingRequestStatus.Accepted, ownerId);
    }

    @Test
    void testFindLendingHistoryInvalidOwnerOrNoAcceptedRequests() {
        int ownerId = 999; // An ID for which no requests exist or none are Accepted

        // Mock the repository to return an empty list for this owner and status
        when(borrowingRequestRepository.findAllRequestsByStatusAndGameOwner(
                eq(BorrowingRequestStatus.Accepted), 
                eq(ownerId)))
                .thenReturn(Collections.emptyList());

        // --- Call the service method ---
        List<BorrowingRequest> result = borrowingManagementService.findLendingHistory(ownerId);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected empty history for invalid owner or no accepted requests");

        verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndGameOwner(BorrowingRequestStatus.Accepted, ownerId);

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
        verify(borrowingRequestRepository, times(1)).findById(requestId);
    }

    @Test
    public void testGetBorrowingRequestByIdInvalid() {
        int requestId = 100;
        when(borrowingRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.getBorrowingRequestById(requestId);
        });

        String expectedMessage = "Borrowing request not found with ID: " + requestId;
        assertEquals(expectedMessage, e.getMessage());
        verify(borrowingRequestRepository, times(1)).findById(requestId);
    }
     
    @Test
    public void testFindGameCopyLendingStatus_GameCopyPassedDirectly_NoAcceptedRequest() {
        int gameCopyId = 1;
        GameCopy gameCopy = new GameCopy();
        gameCopy.setId(gameCopyId);

        List<BorrowingRequest> requests = new ArrayList<>();
        BorrowingRequest deliveredRequest = new BorrowingRequest();
        deliveredRequest.setStatus(BorrowingRequestStatus.Delivered);
        deliveredRequest.setGameCopy(gameCopy);
        BorrowingRequest rejectedRequest = new BorrowingRequest();
        rejectedRequest.setStatus(BorrowingRequestStatus.Rejected);
        rejectedRequest.setGameCopy(gameCopy);
        requests.add(deliveredRequest);
        requests.add(rejectedRequest);

        when(borrowingRequestRepository.findByGameCopy(gameCopy)).thenReturn(requests);

        BorrowingRequest result = borrowingManagementService.findGameCopyLendingStatus(gameCopy);
        
        assertNull(result, "Should return null when no accepted borrowing request exists");

        verify(borrowingRequestRepository, times(1)).findByGameCopy(gameCopy);
    }

    @Test
    public void testFindGameCopyLendingStatus_GameCopyPassedDirectly_AcceptedRequestFound() {
        int gameCopyId = 1;
        GameCopy gameCopy = new GameCopy();
        gameCopy.setId(gameCopyId);

        List<BorrowingRequest> requests = new ArrayList<>();
        BorrowingRequest acceptedRequest = new BorrowingRequest();
        acceptedRequest.setStatus(BorrowingRequestStatus.Accepted);
        acceptedRequest.setGameCopy(gameCopy);
        BorrowingRequest deliveredRequest = new BorrowingRequest();
        deliveredRequest.setStatus(BorrowingRequestStatus.Delivered);
        deliveredRequest.setGameCopy(gameCopy);
        requests.add(acceptedRequest);
        requests.add(deliveredRequest);

        when(borrowingRequestRepository.findByGameCopy(gameCopy)).thenReturn(requests);

        BorrowingRequest result = borrowingManagementService.findGameCopyLendingStatus(gameCopy);
        
        assertNotNull(result, "Should return the accepted borrowing request");
        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository, times(1)).findByGameCopy(gameCopy);
    }
}