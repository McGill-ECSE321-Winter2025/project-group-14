package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gamenight.dto.ErrorDto;
import ca.mcgill.ecse321.gamenight.dto.GameCopyRequestDto;
import ca.mcgill.ecse321.gamenight.dto.GameCopyResponseDto;
import ca.mcgill.ecse321.gamenight.dto.GameRequestDto;
import ca.mcgill.ecse321.gamenight.dto.GameResponseDto;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class GameManagementIntegrationTest {

    @Autowired
	private TestRestTemplate client;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GameCopyRepository gameCopyRepository;
    @Autowired
    private GameOwnerRepository gameOwnerRepository;
    @Autowired
    private PersonRepository personRepository;
    
    private int createGameId;
    private static final String VALID_NAME = "Uno";
	private static final String VALID_DESCRIPTION = "A multiplayer card game";
    private static final String NEW_NAME = "Monopoly";
    private static final String NEW_DESCRIPTION = "A board game";

    private Game createdGame2;
    
    private GameOwner createdGameOwner;
    
    private int createGameCopyId;
    private static final String VALID_EMAIL = "bob@gmail.com";
    private static final String VALID_PERSON_NAME = "Bob";
    private static final String VALID_PASSWORD = "password123";
    private static final String VALID_GAME_COPY_DESCRIPTION = "Perfect condition";


    @BeforeAll
    public void setup() {
        gameCopyRepository.deleteAll();
        gameRepository.deleteAll();
        gameOwnerRepository.deleteAll();
        personRepository.deleteAll();

        //create owner to use for game copies
        Person person = personRepository.save(new Person(VALID_EMAIL, VALID_PASSWORD, VALID_PERSON_NAME));
        createdGameOwner = gameOwnerRepository.save(new GameOwner(person));
    }

    @AfterAll
    public void cleanup() {
        gameCopyRepository.deleteAll();
        gameRepository.deleteAll();
        gameOwnerRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    @Order(0)
	public void testCreateValidGame() {
        GameRequestDto body = new GameRequestDto(VALID_NAME, VALID_DESCRIPTION);

        ResponseEntity<GameResponseDto> response = client.postForEntity("/games/", body, GameResponseDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().getId() > 0, "the ID should be a positive int");
        this.createGameId = response.getBody().getId();
		assertEquals(body.getName(), response.getBody().getName());
		assertEquals(body.getDescription(), response.getBody().getDescription());
    }

    @Test
    @Order(1)
	public void testCreateGameWithNoName() {
        GameRequestDto body = new GameRequestDto("", "A multiplayer card game");

        ResponseEntity<ErrorDto> response = client.postForEntity("/games/", body, ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertIterableEquals(
				List.of("Game must have a name"),
				response.getBody().getErrors());
    }

    @Test
    @Order(2)
	public void testFindGameByValidId() {
        String url = String.format("/games/%d", createGameId);

        ResponseEntity<GameResponseDto> response = client.getForEntity(url, GameResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(createGameId, response.getBody().getId());
		assertEquals(VALID_NAME, response.getBody().getName());
		assertEquals(VALID_DESCRIPTION, response.getBody().getDescription());
    }

    @Test
    @Order(3)
	public void testUpdateGame() {
        GameRequestDto gameRequest = new GameRequestDto(NEW_NAME, NEW_DESCRIPTION);
        String url = String.format("/games/%d", createGameId);

        ResponseEntity<GameResponseDto> response = client.exchange(url, HttpMethod.PUT, new HttpEntity<>(gameRequest), GameResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(createGameId, response.getBody().getId());
		assertEquals(NEW_NAME, response.getBody().getName());
		assertEquals(NEW_DESCRIPTION, response.getBody().getDescription());
    }

    @Test
    @Order(4)
    public void testGetMultipleGames() {
        this.createdGame2 = new Game("Scrabble", "Another game");
        gameRepository.save(createdGame2);

        ResponseEntity<GameResponseDto[]> response = client.getForEntity("/games", GameResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
        
        GameResponseDto game1 = response.getBody()[0];
		assertEquals(createGameId, game1.getId());
        assertEquals(NEW_NAME, game1.getName());
		assertEquals(NEW_DESCRIPTION, game1.getDescription());
        
        GameResponseDto game2 = response.getBody()[1];
		assertEquals(createdGame2.getId(), game2.getId());
        assertEquals("Scrabble", game2.getName());
		assertEquals("Another game", game2.getDescription());
    }

    @Test
    @Order(5)
	public void testCreateValidGameCopy() {
        GameCopyRequestDto body = new GameCopyRequestDto(createGameId, createdGameOwner.getId(), VALID_GAME_COPY_DESCRIPTION);

        ResponseEntity<GameCopyResponseDto> response = client.postForEntity("/game-copies/", body, GameCopyResponseDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().getId() > 0, "the ID should be a positive int");
        this.createGameCopyId = response.getBody().getId();
		assertEquals(body.getGameId(), response.getBody().getGame().getId());
		assertEquals(body.getDescription(), response.getBody().getDescription());
    }

    @Test
    @Order(6)
	public void testGetCreatedGameCopy() {

        ResponseEntity<GameCopyResponseDto> response = client.getForEntity("/game-copies/" + createGameCopyId, GameCopyResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().getId() > 0, "the ID should be a positive int");
		assertEquals(createGameId, response.getBody().getGame().getId());
		assertEquals(VALID_GAME_COPY_DESCRIPTION, response.getBody().getDescription());
    }

    @Test
    @Order(7)
	public void testUpdateGameCopy() {
        GameCopyRequestDto body = new GameCopyRequestDto(createGameId, createdGameOwner.getId(), "Medium condition");
        String url = String.format("/game-copies/%d", createGameCopyId);

        ResponseEntity<GameCopyRequestDto> response = client.exchange(url, HttpMethod.PUT, new HttpEntity<>(body), GameCopyRequestDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(body.getDescription(), response.getBody().getDescription());
    }

    @Test
    @Order(8)
    public void testGetMultipleGameCopiesForOwner() {
        GameCopy newGameCopy = new GameCopy("Missing one piece", createdGame2, createdGameOwner);
        gameCopyRepository.save(newGameCopy);
        String url = String.format("/game-copies?owner_id=%d", createdGameOwner.getId());
        
        ResponseEntity<GameCopyResponseDto[]> response = client.getForEntity(url, GameCopyResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
        
        GameCopyResponseDto gameCopy1 = response.getBody()[0];
        assertEquals(createGameCopyId, gameCopy1.getId());
		assertEquals(createGameId, gameCopy1.getGame().getId());
        
        GameCopyResponseDto gameCopy2 = response.getBody()[1];
        assertEquals(newGameCopy.getId(), gameCopy2.getId());
        assertEquals(createdGame2.getId(), gameCopy2.getGame().getId());
    }

    @Test
    @Order(9)
    public void testDeleteGameCopy() {
        String url = String.format("/game-copies/%d", createGameCopyId);

        ResponseEntity<Void> response = client.exchange(url, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(null, response.getBody());
        assertTrue(gameCopyRepository.findById(createGameCopyId).isEmpty());
    }
}
