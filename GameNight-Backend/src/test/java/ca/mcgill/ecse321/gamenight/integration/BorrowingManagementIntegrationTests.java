package ca.mcgill.ecse321.gamenight.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    public void testsendValidBorrowingRequest() {
        
        Date startTime = Date.valueOf("2025-01-05");
        Date endTime = Date.valueOf("2025-01-10");

        BorrowingRequestRequestDto request = new BorrowingRequestRequestDto(startTime, endTime, validSenderId, validGameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> response = 
                client.postForEntity("/borrowingRequests", request, BorrowingRequestResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        BorrowingRequestResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getSendTime());
    }


}
