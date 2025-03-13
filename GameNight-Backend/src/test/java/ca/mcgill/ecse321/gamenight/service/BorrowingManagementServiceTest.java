package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.exceptions.ReqGameCopyNotFoundException;
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
import jakarta.persistence.EntityNotFoundException;

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
    
    private Person senderPerson;
    private Person ownerPerson;
    private BorrowingRequest request;
    private Game game;

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
        }
    
    @Test
    public void sendValidBorrowingRequestTest() {
        int gameCopyId = 10;
        int senderId = 5;
        Date startTime = Date.valueOf("2025-03-12");
        Date endTime = Date.valueOf("2025-03-15");
        
        //Game and Gameowner
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
        
        //Sender
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
        
        BorrowingRequest result = borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        
        assertNotNull(result);
        assertEquals(BorrowingRequestStatus.Delivered, result.getStatus());
        assertEquals("uno", result.getGameCopy().getGame().getName());
        assertEquals("john", result.getSender().getPerson().getName());

        assertNotNull(result.getSendTime());
        long currentTime= System.currentTimeMillis();
        long sendTimeMillis = result.getSendTime().getTime();
        assertTrue(Math.abs(currentTime - sendTimeMillis)< 2000, "Send time must be within 2 sec of current time");
        verify(emailService, times(1)).sendBorrowingRequestEmail(
            eq("hamza@example.com"),
            eq(senderPerson),
            eq("uno")
        );
    }
    
    @Test
    public void sendBorrowingRequestInvalidGameCopyTest() {
        int gameCopyId = 10;
        int senderId = 5;
        Date startTime = Date.valueOf("2025-03-12");
        Date endTime = Date.valueOf("2025-03-15");
        
        when(gameCopyRepository.findById(gameCopyId)).thenReturn(Optional.empty());
        
        Exception e = assertThrows(ReqGameCopyNotFoundException.class, () -> {
            borrowingManagementService.sendBorrowingRequest(gameCopyId, senderId, startTime, endTime);
        });
        
        String expectedMessage = "No active borrowing request found for Game Copy ID: " + gameCopyId;
        assertEquals(expectedMessage, e.getMessage());
    }
    
    @Test
    public void sendBorrowingRequestInvalidSenderTest() {
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
        
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
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

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(request, BorrowingRequestStatus.Accepted);
        
        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository, times(2)).save(any(BorrowingRequest.class));

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

        BorrowingRequest result = borrowingManagementService.respondToBorrowingRequest(request, BorrowingRequestStatus.Rejected);
        
        assertEquals(BorrowingRequestStatus.Rejected, result.getStatus());
        verify(borrowingRequestRepository, times(2)).save(any(BorrowingRequest.class));
    }

    @Test
    void testRespondToBorrowingRequest_InvalidRequest() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> 
            borrowingManagementService.respondToBorrowingRequest(null, BorrowingRequestStatus.Accepted)
        );
        
        assertEquals("Invalid borrowing request or missing game details.", exception.getMessage());
    }

    @Test
    void testUpdateBorrowingRequestToAccepted(){
        BorrowingRequest request = new BorrowingRequest();
        request.setId(1);
        request.setStatus(BorrowingRequestStatus.Delivered);

        when(borrowingRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.updateBorrowingRequestStatus(request, BorrowingRequestStatus.Accepted);
        assertEquals(BorrowingRequestStatus.Accepted, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(request); // used to make sure the code would be updating to the database
    }

    @Test
    void testUpdateBorrowingRequestToRejected(){
        BorrowingRequest request = new BorrowingRequest();
        request.setId(1);
        request.setStatus(BorrowingRequestStatus.Delivered);

        when(borrowingRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(borrowingRequestRepository.save(any(BorrowingRequest.class))).thenAnswer(i -> i.getArgument(0));

        BorrowingRequest result = borrowingManagementService.updateBorrowingRequestStatus(request, BorrowingRequestStatus.Rejected);
        assertEquals(BorrowingRequestStatus.Rejected, result.getStatus());
        verify(borrowingRequestRepository, times(1)).save(request); // used to make sure the code would be updating to the database
    }

    @Test
    void testUpdateBorrowingRequestThatDoesNotExist(){
        BorrowingRequest request = new BorrowingRequest();
        when(borrowingRequestRepository.findById(383)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            borrowingManagementService.updateBorrowingRequestStatus(request, BorrowingRequestStatus.Accepted);
    });
    verify(borrowingRequestRepository, never()).save(any(BorrowingRequest.class));
    }
}