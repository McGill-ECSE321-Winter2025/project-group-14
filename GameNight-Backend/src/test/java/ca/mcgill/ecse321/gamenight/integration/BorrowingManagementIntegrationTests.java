package ca.mcgill.ecse321.gamenight.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.sql.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestRequestDto;
import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestResponseDto;
import ca.mcgill.ecse321.gamenight.exceptions.EmailSendingFailedException;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.BorrowingRequestRepository;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class BorrowingManagementIntegrationTests {
    @Autowired
    private TestRestTemplate client;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameOwnerRepository gameOwnerRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private BorrowingRequestRepository borrowingRequestRepository;

    private JavaMailSender eMailSender;


    private int validSenderId;
    private int validGameCopyId;
    private GameOwner createdGameOwner;

    private static final String VALID_EMAIL1 = "johnash@gmail.com";
    private static final String VALID_EMAIL2 = "sender@gmail.com";
    private static final String VALID_PASSWORD1 = "1234RE";
    private static final String VALID_NAME1 = "John Nashville";
    private static final String VALID_GAMENAME = "Uno";
    private static final String VALID_DESCRIPTION = "card game";
    private static final String VALID_GAMECOPYDESCRIPTION =  "Good condition";
    private static final String VALID_PASSWORD2 = "1234RE";
    private static final String VALID_NAME2 = "jane doe";
    private static final Date START_TIME = Date.valueOf("2025-01-05");
    private static final Date END_TIME = Date.valueOf("2025-01-10");



    @BeforeAll
    public void setup(){
        gameCopyRepository.deleteAll();
        gameRepository.deleteAll();
        gameOwnerRepository.deleteAll();
        personRepository.deleteAll();
        playerRepository.deleteAll();

        Person ownerPerson = personRepository.save(new Person(VALID_EMAIL1, VALID_PASSWORD1, VALID_NAME1));
        createdGameOwner = gameOwnerRepository.save(new GameOwner(ownerPerson));
        Game game = new Game(VALID_GAMENAME, VALID_DESCRIPTION);
        game = gameRepository.save(game);
        GameCopy gameCopy = new GameCopy(VALID_GAMECOPYDESCRIPTION, game, createdGameOwner);
        gameCopy = gameCopyRepository.save(gameCopy);
        validGameCopyId = gameCopy.getId();
        Person senderPerson = personRepository.save(new Person(VALID_EMAIL2, VALID_PASSWORD2, VALID_NAME2));
        Player sender = new Player(senderPerson);
        sender = playerRepository.save(sender);
        validSenderId = sender.getId();
    }
    @AfterAll
    public void cleanup(){
        borrowingRequestRepository.deleteAll();
        gameCopyRepository.deleteAll();
        playerRepository.deleteAll();
        gameOwnerRepository.deleteAll();
        gameRepository.deleteAll();
        personRepository.deleteAll();
    }
    @Test
    @Order(0)
    public void testSendValidBorrowingRequest() {
        
        BorrowingRequestRequestDto request = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId, validGameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> response = 
                client.postForEntity("/borrowingRequests", request, BorrowingRequestResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        BorrowingRequestResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getSendTime());
    }

    @Test
    @Order(1)
    public void testSendBorrowingRequestInvalidGameCopyTest(){
        BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId, 99999);
        ResponseEntity<String> response = client.postForEntity("/borrowingRequests", requestDto, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(2)
    public void testSendBorrowingRequestInvalidSenderTest(){
        BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, 99999, validGameCopyId);
        ResponseEntity<String> response = client.postForEntity("/borrowingRequests", requestDto, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test 
    @Order(3)
    public void testFindDeliveredRequestsForBorrowerValid(){

        BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId, validGameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> response = client.postForEntity("/borrowingRequests", requestDto, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
       
        String url = String.format("/borrowingRequests/%d/status/delivered", validSenderId);
        ResponseEntity<BorrowingRequestResponseDto[]> getResponse = client.getForEntity(url, BorrowingRequestResponseDto[].class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        BorrowingRequestResponseDto[] requests = getResponse.getBody();
        assertNotNull(requests);
        assertTrue(requests.length > 0);
    }
    

    @Test 
    @Order(4)
    public void testFindDeliveredRequestsForBorrowerInvalid(){
        
        String url = String.format("/borrowingRequests/%d/status/delivered", 9999);
        ResponseEntity<BorrowingRequestResponseDto[]> getResponse = client.getForEntity(url, BorrowingRequestResponseDto[].class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        BorrowingRequestResponseDto[] requests = getResponse.getBody();
        assertNotNull(requests);
        assertEquals(0, requests.length, "Expected no delivered requests for borrower ID " + 9999);
    }

    @Test
    @Order(5)
    public void testFindRejectedRequestsForBorrowerValid(){
    
        BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId, validGameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> postResponse = client.postForEntity("/borrowingRequests", requestDto, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        BorrowingRequestResponseDto createdRequest = postResponse.getBody();
        assertNotNull(createdRequest);
    
        String updateUrl = String.format("/borrowingRequests/%d/status?status=Rejected", createdRequest.getId());
        ResponseEntity<BorrowingRequestResponseDto> updateResponse = client.exchange(updateUrl, 
                org.springframework.http.HttpMethod.PUT, null, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode()); 
    }

    @Test
    @Order(6)
    public void testFindRejectedRequestsForBorrowerInvalid(){
        int invalidBorrowerId = 77777;
        String url = String.format("/borrowingRequests/%d/status/rejected", invalidBorrowerId);
        ResponseEntity<BorrowingRequestResponseDto[]> response = client.getForEntity(url, BorrowingRequestResponseDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowingRequestResponseDto[] requests = response.getBody();
        assertNotNull(requests);
        assertEquals(0, requests.length);
    }

    @Test
    @Order(7)
    public void testFindAcceptedRequestsForBorrowerValid(){
        BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId, validGameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> postResponse = client.postForEntity("/borrowingRequests", requestDto, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
        BorrowingRequestResponseDto createdRequest = postResponse.getBody();
        assertNotNull(createdRequest);
    
        String updateUrl = String.format("/borrowingRequests/%d/status?status=Accepted", createdRequest.getId());
        ResponseEntity<BorrowingRequestResponseDto> updateResponse = client.exchange(updateUrl,
                org.springframework.http.HttpMethod.PUT, null, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    }

    @Test
    @Order(8)
    public void testFindAcceptedRequestsForBorrowerInvalid(){
        String url = String.format("/borrowingRequests/%d/status/accepted", 77777);
        ResponseEntity<BorrowingRequestResponseDto[]> response = client.getForEntity(url, BorrowingRequestResponseDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowingRequestResponseDto[] requests = response.getBody();
        assertNotNull(requests);
        assertEquals(0, requests.length);
    }

    @Test
    @Order(9)
    public void testGetBorrowingRequestByIdValid() {
        BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId, validGameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> createResponse = client.postForEntity("/borrowingRequests", requestDto, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        BorrowingRequestResponseDto created = createResponse.getBody();
        assertNotNull(created);

        String url = String.format("/borrowingRequests/%d", created.getId());
        ResponseEntity<BorrowingRequestResponseDto> getResponse = client.getForEntity(url, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        BorrowingRequestResponseDto fetched = getResponse.getBody();
        assertNotNull(fetched);
        assertEquals(created.getId(), fetched.getId());
    }

    @Test
    @Order(10)
    public void testGetBorrowingRequestByIdInvalid() {
        String url = String.format("/borrowingRequests/%d", 99999);
        ResponseEntity<String> response = client.getForEntity(url, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    @Test
    @Order(11)
    public void testRespondToBorrowingRequestAccepted(){
        BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId, validGameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> createResponse = client.postForEntity("/borrowingRequests", requestDto, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        BorrowingRequestResponseDto createdRequest = createResponse.getBody();
        assertNotNull(createdRequest);
        String updateUrl = String.format("/borrowingRequests/%d/status?status=Accepted", createdRequest.getId());
        ResponseEntity<BorrowingRequestResponseDto> response = client.exchange(updateUrl, org.springframework.http.HttpMethod.PUT, null, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowingRequestResponseDto updated = response.getBody();
        assertNotNull(updated);
    }

    @Test
    @Order(12)
    public void testRespondToBorrowingRequestRejected(){
        BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId, validGameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> createResponse = client.postForEntity("/borrowingRequests", requestDto, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        BorrowingRequestResponseDto createdRequest = createResponse.getBody();
        assertNotNull(createdRequest);
        String updateUrl = String.format("/borrowingRequests/%d/status?status=Rejected", createdRequest.getId());
        ResponseEntity<BorrowingRequestResponseDto> response = client.exchange(updateUrl, org.springframework.http.HttpMethod.PUT, null, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowingRequestResponseDto updated = response.getBody();
        assertNotNull(updated);
    }

    @Test
    @Order(13)
    void testUpdateBorrowingRequestToAccepted(){
        BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId, validGameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> createResponse = client.postForEntity("/borrowingRequests", requestDto, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        BorrowingRequestResponseDto createdRequest = createResponse.getBody();
        assertNotNull(createdRequest);
        String updateUrlRejected = String.format("/borrowingRequests/%d/status?status=Rejected", createdRequest.getId());
        ResponseEntity<BorrowingRequestResponseDto> rejectedResponse = client.exchange(updateUrlRejected, org.springframework.http.HttpMethod.PUT, null, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.OK, rejectedResponse.getStatusCode());
        String updateUrlAccepted = String.format("/borrowingRequests/%d/status?status=Accepted", createdRequest.getId());
        ResponseEntity<BorrowingRequestResponseDto> acceptedResponse = client.exchange(updateUrlAccepted, org.springframework.http.HttpMethod.PUT, null, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.OK, acceptedResponse.getStatusCode());
        BorrowingRequestResponseDto updated = acceptedResponse.getBody();
        assertNotNull(updated);

    }

    @Test
    @Order(14)
    void testUpdateBorrowingRequestToRejected(){
        BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId, validGameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> createResponse = client.postForEntity("/borrowingRequests", requestDto, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        BorrowingRequestResponseDto createdRequest = createResponse.getBody();
        assertNotNull(createdRequest);
        String updateUrlAccepted = String.format("/borrowingRequests/%d/status?status=Accepted", createdRequest.getId());
        ResponseEntity<BorrowingRequestResponseDto> acceptedResponse = client.exchange(updateUrlAccepted, org.springframework.http.HttpMethod.PUT, null, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.OK, acceptedResponse.getStatusCode());
        String updateUrlRejected = String.format("/borrowingRequests/%d/status?status=Rejected", createdRequest.getId());
        ResponseEntity<BorrowingRequestResponseDto> rejectedResponse = client.exchange(updateUrlRejected, org.springframework.http.HttpMethod.PUT, null, BorrowingRequestResponseDto.class);
        assertEquals(HttpStatus.OK, rejectedResponse.getStatusCode());
        BorrowingRequestResponseDto updated = rejectedResponse.getBody();
        assertNotNull(updated);

    }

    @Test
    @Order(15)
    void testUpdateBorrowingRequestThatDoesNotExist(){
        String updateUrl = String.format("/borrowingRequests/%d/status?status=Accepted", 99999);
        ResponseEntity<String> response = client.exchange(updateUrl, org.springframework.http.HttpMethod.PUT, null, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    }

}

