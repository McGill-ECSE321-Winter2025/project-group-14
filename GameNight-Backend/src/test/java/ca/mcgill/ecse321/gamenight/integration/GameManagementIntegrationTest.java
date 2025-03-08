package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gamenight.dto.ErrorDto;
import ca.mcgill.ecse321.gamenight.dto.GameRequestDto;
import ca.mcgill.ecse321.gamenight.dto.GameResponseDto;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class GameManagementIntegrationTest {

    @Autowired
	private TestRestTemplate client;
    @Autowired
    private GameRepository gameRepository;
	
    private static final String VALID_NAME = "Uno";
	private static final String VALID_DESCRIPTION = "A multiplayer card game";

    @AfterAll
    public void cleanup() {
        gameRepository.deleteAll();
    }

    @Test
	public void testCreateValidGame() {
        GameRequestDto body = new GameRequestDto(VALID_NAME, VALID_DESCRIPTION);

        ResponseEntity<GameResponseDto> response = client.postForEntity("/games/", body, GameResponseDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().getId() > 0, "the ID should be a positive int");
		assertEquals(body.getName(), response.getBody().getName());
		assertEquals(body.getDescription(), response.getBody().getDescription());
    }

    @Test
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
	public void testFindGameByValidId() {
        Game game = new Game(VALID_NAME, VALID_DESCRIPTION);
        gameRepository.save(game);
        java.util.Optional<Game> foundG = gameRepository.findById(game.getId());
        assertTrue(foundG.isPresent());
        String url = String.format("/games/%d", game.getId());

        ResponseEntity<GameResponseDto> response = client.getForEntity(url, GameResponseDto.class);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(game.getId(), response.getBody().getId());
		assertEquals(VALID_NAME, response.getBody().getName());
		assertEquals(VALID_DESCRIPTION, response.getBody().getDescription());
    }
}
