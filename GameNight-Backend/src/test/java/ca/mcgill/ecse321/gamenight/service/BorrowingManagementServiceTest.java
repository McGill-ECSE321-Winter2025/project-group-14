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
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.junit.jupiter.MockitoExtension; 
import org.junit.jupiter.api.extension.ExtendWith; 


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
@ExtendWith(MockitoExtension.class) 
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

    private Person senderPerson;
    private Person ownerPerson;
    private BorrowingRequest request;
    private Game game;


    @Test
    public void testSendValidBorrowingRequest() {
        int gameCopyId = 10;
        int senderId = 5;
        Date startTime = Date.valueOf("2025-03-12"); 
        Date endTime = Date.valueOf("2025-03-15");

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

        Person senderPerson = new Person();
        senderPerson.setName("john");
        senderPerson.setEmailAddress("john@example.com");
        Player sender = new Player();
        sender.setId(senderId);
        sender.setPerson(senderPerson); 

        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.of(gameCopy));
        when(playerRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class)))
                .thenAnswer(invocation -> {
                    BorrowingRequest savedRequest = invocation.getArgument(0);
                    savedRequest.setId(120); 
                    return savedRequest;
                });

        BorrowingRequest result = borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);

        assertNotNull(result);
        assertEquals(BorrowingRequestStatus.Delivered, result.getStatus()); 
        assertEquals("uno", result.getGameCopy().getGame().getName());
        assertEquals("john", result.getSender().getPerson().getName());
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());
        assertEquals(120, result.getId());

        assertNotNull(result.getSendTime());
        long currentTime = System.currentTimeMillis();
        long sendTimeMillis = result.getSendTime().getTime();
        assertTrue(Math.abs(currentTime - sendTimeMillis) < 5000, "Send time must be within 5 sec of current time"); 

        verify(borrowingRequestRepository, times(1)).save(any(BorrowingRequest.class));
        verify(emailService, times(1)).sendBorrowingRequestEmail(
                eq("hamza@example.com"),
                eq(senderPerson), 
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
        doThrow(new EmailSendingFailedException("Failed to send email"))
                .when(emailService).sendBorrowingRequestEmail(anyString(), any(Person.class), anyString());

        EmailSendingFailedException e = assertThrows(EmailSendingFailedException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });

        assertEquals("Failed to send email", e.getMessage());
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
        when(playerRepository.findById(senderId)).thenReturn(Optional.empty()); 

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
        request.setStatus(BorrowingRequestStatus.Delivered); 

        Game game = new Game();
        game.setName("Uno");
        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(game);

        Person ownerPerson = new Person(); 
        ownerPerson.setName("Owner");
        ownerPerson.setEmailAddress("owner@example.com");
        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson); 
        gameCopy.setGameOwner(gameOwner); 
        request.setGameCopy(gameCopy);

        Person senderPerson = new Person();
        senderPerson.setName("Sender");
        senderPerson.setEmailAddress("sender@example.com");
        Player sender = new Player();
        sender.setId(5); 
        sender.setPerson(senderPerson); 
        request.setSender(sender);

        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(request, BorrowingRequestStatus.Accepted);

        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(request); 
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
        request.setStatus(BorrowingRequestStatus.Delivered); 

        Game game = new Game();
        game.setName("Uno");
        GameCopy gameCopy = new GameCopy();
        gameCopy.setGame(game);

        Person ownerPerson = new Person(); 
        ownerPerson.setName("Owner");
        ownerPerson.setEmailAddress("owner@example.com");
        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(ownerPerson); 
        gameCopy.setGameOwner(gameOwner); 
        request.setGameCopy(gameCopy);

        Person senderPerson = new Person();
        senderPerson.setName("Sender");
        senderPerson.setEmailAddress("sender@example.com");
        Player sender = new Player();
        sender.setId(5); 
        sender.setPerson(senderPerson); 
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
        int requestId = 1;
        BorrowingRequest request = new BorrowingRequest();
        request.setId(requestId);
        request.setStatus(BorrowingRequestStatus.Accepted); 

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

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(request, BorrowingRequestStatus.Delivered);

        assertEquals(BorrowingRequestStatus.Delivered, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(request);
        verify(emailService, never()).sendRequestAcceptedEmail(anyString(), anyString(), anyString());
        verify(emailService, never()).sendRequestRejectedEmail(anyString(), anyString(), anyString());
    }

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
        BorrowingRequest request = new BorrowingRequest(); 
        request.setId(requestId);

        when(borrowingRequestRepository.findById(requestId)).thenReturn(Optional.empty()); 

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> {
            borrowingManagementService.updateBorrowingRequestStatus(request, BorrowingRequestStatus.Accepted); 
        });

        assertEquals("Borrowing request not found", e.getMessage()); 
        verify(borrowingRequestRepository, times(1)).findById(requestId); 
        verify(borrowingRequestRepository, never()).save(any(BorrowingRequest.class)); 
    }

    @Test
    void testFindDeliveredBorrowingRequestsForBorrowerValid() {
        int borrowerId = 1;
        BorrowingRequest req1 = mock(BorrowingRequest.class);
        // REMOVED Unnecessary Stubbing: when(req1.getStatus()).thenReturn(BorrowingRequestStatus.Delivered);
        BorrowingRequest req2 = mock(BorrowingRequest.class);
        // REMOVED Unnecessary Stubbing: when(req2.getStatus()).thenReturn(BorrowingRequestStatus.Delivered);
        List<BorrowingRequest> deliveredList = Arrays.asList(req1, req2);

        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId))
                .thenReturn(deliveredList);

        List<BorrowingRequest> result = borrowingManagementService.findDeliveredBorrowingRequestsForBorrower(borrowerId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId);
    }

    @Test
    void testFindDeliveredBorrowingRequestsForBorrowerInvalid() {
        int borrowerId = 999;
        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId))
                .thenReturn(Collections.emptyList()); 

        List<BorrowingRequest> result = borrowingManagementService.findDeliveredBorrowingRequestsForBorrower(borrowerId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, borrowerId);
    }

    @Test
    void testFindRejectedBorrowingRequestsForBorrowerValid() {
        int borrowerId = 2;
        BorrowingRequest req1 = mock(BorrowingRequest.class);
        List<BorrowingRequest> rejectedList = Collections.singletonList(req1);

        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, borrowerId))
                .thenReturn(rejectedList);

        List<BorrowingRequest> result = borrowingManagementService.findRejectedBorrowingRequestsForBorrower(borrowerId);
        
        assertNotNull(result);
        assertEquals(1, result.size());
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
         List<BorrowingRequest> acceptedList = Collections.singletonList(req1);

        when(borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, borrowerId))
                .thenReturn(acceptedList); 

        List<BorrowingRequest> result = borrowingManagementService.findAcceptedBorrowingRequestsForBorrower(borrowerId);
        
        assertNotNull(result);
        assertEquals(1, result.size()); 
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

    @Test
    void testFindLendingHistoryValid() {
        int ownerId = 10; 
        BorrowingRequest acceptedRequest = mock(BorrowingRequest.class); 
        when(acceptedRequest.getStatus()).thenReturn(BorrowingRequestStatus.Accepted); 

        List<BorrowingRequest> history = Collections.singletonList(acceptedRequest);

        when(borrowingRequestRepository.findAllRequestsByStatusAndGameOwner(
                eq(BorrowingRequestStatus.Accepted), 
                eq(ownerId))) 
                .thenReturn(history);

        List<BorrowingRequest> result = borrowingManagementService.findLendingHistory(ownerId);

        assertNotNull(result);
        assertEquals(1, result.size(), "Expected one borrowing request in history"); 
        assertEquals(BorrowingRequestStatus.Accepted, result.get(0).getStatus()); 
        
        verify(borrowingRequestRepository, times(1)).findAllRequestsByStatusAndGameOwner(BorrowingRequestStatus.Accepted, ownerId);
    }

    @Test
    void testFindLendingHistoryInvalidOwnerOrNoAcceptedRequests() {
        int ownerId = 999; 

        when(borrowingRequestRepository.findAllRequestsByStatusAndGameOwner(
                eq(BorrowingRequestStatus.Accepted), 
                eq(ownerId)))
                .thenReturn(Collections.emptyList());

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