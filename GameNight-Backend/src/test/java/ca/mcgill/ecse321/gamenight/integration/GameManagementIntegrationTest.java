package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.Assert;

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
import jakarta.validation.constraints.AssertTrue;

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

        aGameOwner = new GameOwner(person);
        aGameOwner.setActive(true);
        gameOwnerRepository.save(aGameOwner);
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

        GameResponseDto game2 = response.getBody()[1];
        assertEquals(createdGame2.getId(), game2.getId());
        assertEquals(createdGame2.getName(), game2.getName());
        assertEquals(createdGame2.getDescription(), game2.getDescription());
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
    public void testFindGameCopiesByGame() {
        Game game = gameRepository.save(new Game("Batman", "A batman game"));
        GameCopy gc1 = gameCopyRepository.save(new GameCopy("Perfect condition", game, aGameOwner));
        GameCopy gc2 = gameCopyRepository.save(new GameCopy("Missing piece", game, aGameOwner));
        GameCopy gc3 = gameCopyRepository.save(new GameCopy("Missing instructions", game, aGameOwner));

        String url = String.format("/game/%d/game-copies", game.getId());
        HttpEntity<?> requestEntity = new HttpEntity<>(authenticationHeaders);

        ResponseEntity<GameCopyResponseDto[]> response = client.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                GameCopyResponseDto[].class);

        assertEquals(3, response.getBody().length);
        GameCopyResponseDto[] gameCopiesQueried = response.getBody();
        assertEquals(gameCopiesQueried[0].getId(), gc1.getId());
        assertEquals(gameCopiesQueried[1].getId(), gc2.getId());
        assertEquals(gameCopiesQueried[2].getId(), gc3.getId());
    }

    @Test
    @Order(10)
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

    // DELIVERABLE 3 ADDITIONS:
    @SuppressWarnings("null")
    @Test
    @Order(10)
    public void testGetRandomGames_withExistingGames() {
        HttpEntity<?> requestEntity = new HttpEntity<>(null);

        ResponseEntity<GameResponseDto[]> response = client.exchange(
                "/public-random-games",
                HttpMethod.GET,
                requestEntity,
                GameResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length <= 10);
        assertTrue(response.getBody().length > 0);
    }

    @SuppressWarnings("null")
    @Test
    @Order(11)
    public void testGetRandomGames_limitIs10() {
        HttpEntity<?> requestEntity = new HttpEntity<>(null);

        ResponseEntity<GameResponseDto[]> response = client.exchange(
                "/public-random-games",
                HttpMethod.GET,
                requestEntity,
                GameResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length <= 10);
    }

    @SuppressWarnings("null")
    @Test
    @Order(12)
    public void testGetRandomGames_randomnessSanityCheck() {
        HttpEntity<?> requestEntity = new HttpEntity<>(null);

        ResponseEntity<GameResponseDto[]> firstCall = client.exchange(
                "/public-random-games",
                HttpMethod.GET,
                requestEntity,
                GameResponseDto[].class);

        ResponseEntity<GameResponseDto[]> secondCall = client.exchange(
                "/public-random-games",
                HttpMethod.GET,
                requestEntity,
                GameResponseDto[].class);

        assertEquals(HttpStatus.OK, firstCall.getStatusCode());
        assertEquals(HttpStatus.OK, secondCall.getStatusCode());

        assertNotNull(firstCall.getBody());
        assertNotNull(secondCall.getBody());

        boolean different = false;
        for (int i = 0; i < Math.min(firstCall.getBody().length, secondCall.getBody().length); i++) {
            if (!(firstCall.getBody()[i].getId() == secondCall.getBody()[i].getId())) {
                different = true;
                break;
            }
        }
        assertTrue(different || firstCall.getBody().length < 2, "Expected at least some randomness");
    }

    @SuppressWarnings("null")
    @Test
    @Order(13)
    public void testFindGameCopiesByGame_whenThreeCopiesExist() {
        // Create a game
        Game game = gameRepository.save(new Game("TestGameWithCopies", "This game has 3 copies"));

        // Create 3 copies for that game
        GameCopy copy1 = new GameCopy("First copy", game, aGameOwner);
        GameCopy copy2 = new GameCopy("Second copy", game, aGameOwner);
        GameCopy copy3 = new GameCopy("Third copy", game, aGameOwner);
        gameCopyRepository.save(copy1);
        gameCopyRepository.save(copy2);
        gameCopyRepository.save(copy3);

        HttpEntity<?> requestEntity = new HttpEntity<>(authenticationHeaders);

        ResponseEntity<GameCopyResponseDto[]> response = client.exchange(
                "/game/" + game.getId() + "/game-copies",
                HttpMethod.GET,
                requestEntity,
                GameCopyResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().length, "Expected exactly 3 game copies");

        List<String> descriptions = List.of("First copy", "Second copy", "Third copy");
        for (GameCopyResponseDto dto : response.getBody()) {
            assertTrue(descriptions.contains(dto.getDescription()), "Unexpected description: " + dto.getDescription());
            assertEquals(game.getId(), dto.getGame().getId(), "All copies should belong to the correct game");
        }
    }

    @Test
    @Order(14)
    public void testGetPublicRandomGames_whenNoGamesExist() {
        // Remove all games (properly)
        gameCopyRepository.deleteAll(); // Required to prevent foreign key issues
        gameRepository.deleteAll();

        HttpEntity<?> requestEntity = new HttpEntity<>(null);

        ResponseEntity<GameResponseDto[]> response = client.exchange(
                "/public-random-games",
                HttpMethod.GET,
                requestEntity,
                GameResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length, "Should return empty when there are no games");
    }

    @Test
    @Order(15)
    public void testGetGameImage() throws IOException {
        // First upload an image
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("game", new GameRequestDto("Test Game With Image", "Description"));

        // Create a mock image file
        byte[] imageBytes = new byte[] { 0x00, 0x01, 0x02 }; // minimal image data
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "test.jpg", "image/jpeg", imageBytes);
        body.add("imageFile", imageFile.getResource());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.addAll(authenticationHeaders);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<GameResponseDto> createResponse = client.exchange(
                "/games",
                HttpMethod.POST,
                requestEntity,
                GameResponseDto.class);

        int gameWithImageId = createResponse.getBody().getId();

        String url = String.format("/games/%d/image", gameWithImageId);
        HttpEntity<?> getRequestEntity = new HttpEntity<>(authenticationHeaders);

        ResponseEntity<byte[]> response = client.exchange(
                url,
                HttpMethod.GET,
                getRequestEntity,
                byte[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

}