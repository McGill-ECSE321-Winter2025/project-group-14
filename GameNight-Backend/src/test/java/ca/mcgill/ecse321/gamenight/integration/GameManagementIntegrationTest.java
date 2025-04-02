package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

    private HttpHeaders authenticationHeaders = new HttpHeaders();

    private int createdGame1Id;
    private static final String VALID_NAME = "Uno";
    private static final String VALID_DESCRIPTION = "A multiplayer card game";
    private static final String NEW_NAME = "Monopoly";
    private static final String NEW_DESCRIPTION = "A board game";

    private Game createdGame2;

    private GameOwner aGameOwner;

    private int createdGameCopyId;
    private static final String VALID_GAME_COPY_DESCRIPTION = "Perfect condition";

    @BeforeAll
    public void setup() {
        gameCopyRepository.deleteAll();
        gameRepository.deleteAll();
        gameOwnerRepository.deleteAll();
        personRepository.deleteAll();

        // create the logged in user
        Person person = personRepository.save(new Person("bob@gmail.com", "password123", "Bob"));
        aGameOwner = gameOwnerRepository.save(new GameOwner(person));
        authenticationHeaders.set("User-Id", String.valueOf(person.getId()));
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

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("game", new GameRequestDto(VALID_NAME, VALID_DESCRIPTION));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.addAll(authenticationHeaders);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<GameResponseDto> response = client.exchange(
                "/games",
                HttpMethod.POST,
                requestEntity,
                GameResponseDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getId() > 0, "the ID should be a positive int");
        this.createdGame1Id = response.getBody().getId();
        assertEquals(VALID_NAME, response.getBody().getName());
        assertEquals(VALID_DESCRIPTION, response.getBody().getDescription());
        assertEquals(0, response.getBody().getRating());
    }

    @Test
    @Order(1)
    public void testCreateGameWithNoName() {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("game", new GameRequestDto("", "A multiplayer card game"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.addAll(authenticationHeaders);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<ErrorDto> response = client.exchange(
                "/games",
                HttpMethod.POST,
                requestEntity,
                ErrorDto.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertIterableEquals(
                List.of("Game must have a name"),
                response.getBody().getErrors());
    }

    @Test
    @Order(2)
    public void testFindGameByValidId() {
        String url = String.format("/games/%d", createdGame1Id);
        HttpEntity<?> requestEntity = new HttpEntity<>(authenticationHeaders);

        ResponseEntity<GameResponseDto> response = client.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                GameResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdGame1Id, response.getBody().getId());
        assertEquals(VALID_NAME, response.getBody().getName());
        assertEquals(VALID_DESCRIPTION, response.getBody().getDescription());
        assertEquals(0, response.getBody().getRating());
    }

    @Test
    @Order(3)
    public void testUpdateGame() {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("game", new GameRequestDto(NEW_NAME, NEW_DESCRIPTION));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.addAll(authenticationHeaders);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String url = String.format("/games/%d", createdGame1Id);

        ResponseEntity<GameResponseDto> response = client.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                GameResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(createdGame1Id, response.getBody().getId());
        assertEquals(NEW_NAME, response.getBody().getName());
        assertEquals(NEW_DESCRIPTION, response.getBody().getDescription());
        assertEquals(0, response.getBody().getRating());
    }

    @Test
    @Order(4)
    public void testGetMultipleGames() {
        this.createdGame2 = new Game("Scrabble", "Another game");
        gameRepository.save(createdGame2);
        HttpEntity<?> requestEntity = new HttpEntity<>(authenticationHeaders);

        ResponseEntity<GameResponseDto[]> response = client.exchange(
                "/games",
                HttpMethod.GET,
                requestEntity,
                GameResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);

        GameResponseDto game1 = response.getBody()[0];
        assertEquals(createdGame1Id, game1.getId());
        assertEquals(NEW_NAME, game1.getName());
        assertEquals(NEW_DESCRIPTION, game1.getDescription());
        assertEquals(0, game1.getRating());

        GameResponseDto game2 = response.getBody()[1];
        assertEquals(createdGame2.getId(), game2.getId());
        assertEquals(createdGame2.getName(), game2.getName());
        assertEquals(createdGame2.getDescription(), game2.getDescription());
        assertEquals(0, game2.getRating());
    }

    @Test
    @Order(5)
    public void testCreateValidGameCopy() {
        GameCopyRequestDto body = new GameCopyRequestDto(createdGame1Id, aGameOwner.getPerson().getId(),
                VALID_GAME_COPY_DESCRIPTION);
        HttpEntity<?> requestEntity = new HttpEntity<>(body, authenticationHeaders);

        ResponseEntity<GameCopyResponseDto> response = client.exchange(
                "/game-copies/",
                HttpMethod.POST,
                requestEntity,
                GameCopyResponseDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getId() > 0, "the ID should be a positive int");
        this.createdGameCopyId = response.getBody().getId();
        assertEquals(body.getGameId(), response.getBody().getGame().getId());
        assertEquals(body.getDescription(), response.getBody().getDescription());
    }

    @Test
    @Order(6)
    public void testGetExistingGameCopy() {
        HttpEntity<?> requestEntity = new HttpEntity<>(authenticationHeaders);

        ResponseEntity<GameCopyResponseDto> response = client.exchange(
                "/game-copies/" + createdGameCopyId,
                HttpMethod.GET,
                requestEntity,
                GameCopyResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getId() > 0, "the ID should be a positive int");
        assertEquals(createdGame1Id, response.getBody().getGame().getId());
        assertEquals(VALID_GAME_COPY_DESCRIPTION, response.getBody().getDescription());
    }

    @Test
    @Order(7)
    public void testUpdateGameCopy() {
        GameCopyRequestDto body = new GameCopyRequestDto(createdGame1Id, aGameOwner.getPerson().getId(),
                "Medium condition");
        String url = String.format("/game-copies/%d", createdGameCopyId);
        HttpEntity<?> requestEntity = new HttpEntity<>(body, authenticationHeaders);

        ResponseEntity<GameCopyResponseDto> response = client.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                GameCopyResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Medium condition", response.getBody().getDescription());
    }

    @Test
    @Order(8)
    public void testGetMultipleGameCopiesForOwner() {
        GameCopy newGameCopy = new GameCopy("Missing one piece", createdGame2, aGameOwner);
        gameCopyRepository.save(newGameCopy);
        String url = String.format("/game-copies?owner_id=%d", aGameOwner.getPerson().getId());
        HttpEntity<?> requestEntity = new HttpEntity<>(authenticationHeaders);

        ResponseEntity<GameCopyResponseDto[]> response = client.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                GameCopyResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);

        GameCopyResponseDto gameCopy1 = response.getBody()[0];
        assertEquals(createdGameCopyId, gameCopy1.getId());
        assertEquals(createdGame1Id, gameCopy1.getGame().getId());

        GameCopyResponseDto gameCopy2 = response.getBody()[1];
        assertEquals(newGameCopy.getId(), gameCopy2.getId());
        assertEquals(createdGame2.getId(), gameCopy2.getGame().getId());
    }

    @Test
    @Order(9)
    public void testDeleteGameCopy() {
        String url = String.format("/game-copies/%d", createdGameCopyId);
        HttpEntity<?> requestEntity = new HttpEntity<>(authenticationHeaders);

        ResponseEntity<Void> response = client.exchange(
                url,
                HttpMethod.DELETE,
                requestEntity,
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        assertTrue(gameCopyRepository.findById(createdGameCopyId).isEmpty());
    }

    @Test
    @Order(10)
    public void testGetGameImage() {
        String url = String.format("/games/%d/image", createdGame1Id);
        HttpEntity<?> requestEntity = new HttpEntity<>(authenticationHeaders);

        ResponseEntity<byte[]> response = client.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                byte[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}