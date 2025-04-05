package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import ca.mcgill.ecse321.gamenight.dto.EventRequestDto;
import ca.mcgill.ecse321.gamenight.dto.EventResponseDto;
import ca.mcgill.ecse321.gamenight.dto.GameResponseDto;
import ca.mcgill.ecse321.gamenight.dto.PlayerResponseDto;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.ScheduledGame;
import ca.mcgill.ecse321.gamenight.model.ScheduledGame.Key;
import ca.mcgill.ecse321.gamenight.repo.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class EventManagementIntegrationTest {

    @Autowired
    private TestRestTemplate client;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private ScheduledGameRepository scheduledGameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameReviewRepository reviewRepository;

    @Autowired
    private GameOwnerRepository gameOwnerRepository;

    private int validEventId;
    private int validPlayerId;
    private int validGameId;
    private int validEventIdToDelete;
    private Player validPlayer;
    private Event validEvent;

    private static final String VALID_EVENT_NAME = "Board Game Night";
    private static final String VALID_EVENT_DESC = "Playing all sorts of board games";
    private static final String UPDATED_EVENT_NAME = "Updated Night";
    private static final String UPDATED_EVENT_DESC = "Updated Description";
    private static final Date START_TIME = new Date(System.currentTimeMillis() + 3600000);
    private static final Date END_TIME = new Date(System.currentTimeMillis() + 7200000);

    @BeforeAll
    public void setupDatabase() {
        registrationRepository.deleteAll();
        scheduledGameRepository.deleteAll();

        eventRepository.deleteAll();

        reviewRepository.deleteAll();
        playerRepository.deleteAll();
        gameRepository.deleteAll();
        gameOwnerRepository.deleteAll();
        personRepository.deleteAll();

        Person playerPerson = personRepository.save(new Person("player@email.com", "pass123", "PlayerOne"));
        validPlayer = playerRepository.save(new Player(playerPerson));
        validPlayerId = validPlayer.getId();

        Game newGame = new Game("SomeGame", "A test game");
        gameRepository.save(newGame);
        validGameId = newGame.getId();

        validEvent = eventRepository.save(new Event(VALID_EVENT_NAME, VALID_EVENT_DESC, START_TIME, END_TIME));
        validEventId = validEvent.getId();

        Event eventToDelete = new Event("ToDelete", "This will be deleted", START_TIME, END_TIME);
        eventToDelete = eventRepository.save(eventToDelete);
        validEventIdToDelete = eventToDelete.getId();
    }

    @AfterAll
    public void clearDatabase() {
        registrationRepository.deleteAll();
        scheduledGameRepository.deleteAll();

        eventRepository.deleteAll();

        reviewRepository.deleteAll();
        playerRepository.deleteAll();
        gameRepository.deleteAll();
        personRepository.deleteAll();
    }

    @Test
    @Order(1)
    public void testCreateValidEvent() {
        EventRequestDto requestDto = new EventRequestDto(
                "New Event",
                "Description",
                START_TIME,
                END_TIME);

        ResponseEntity<EventResponseDto> response = client.postForEntity("/events", requestDto, EventResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        EventResponseDto createdEvent = response.getBody();
        assertNotNull(createdEvent, "Expected a non-null EventResponseDto in the response.");

        assertEquals("New Event", createdEvent.getName(), "Event name should match the request.");
        assertEquals("Description", createdEvent.getDescription(), "Description should match the request.");
        assertEquals(START_TIME, createdEvent.getStartTime(), "Start time should match the request.");
        assertEquals(END_TIME, createdEvent.getEndTime(), "End time should match the request.");

        assertTrue(createdEvent.getId() > 0, "Newly created event should have a valid ID > 0.");
    }

    @Test
    @Order(2)
    public void testCreateEventInvalidName() {
        EventRequestDto requestDto = new EventRequestDto("", "desc", START_TIME, END_TIME);
        ResponseEntity<String> response = client.postForEntity("/events", requestDto, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(
                responseBody.contains("Event name cannot be null or empty"),
                "Expected error message not found in the response body.");
    }

    @Test
    @Order(3)
    public void testCreateEventInvalidTimes() {
        Date invalidEndTime = new Date(START_TIME.getTime() - 10000);
        EventRequestDto requestDto = new EventRequestDto("TimeFail", "desc", START_TIME, invalidEndTime);
        ResponseEntity<String> response = client.postForEntity("/events", requestDto, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertTrue(
                responseBody.contains("Event end time cannot be before the start time."),
                "Expected error message not found in the response body.");
    }

    @Test
    @Order(4)
    public void testGetEventByIdValid() {
        String url = String.format("/events/%d", validEventId);

        ResponseEntity<EventResponseDto> response = client.getForEntity(url, EventResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        EventResponseDto eventDto = response.getBody();
        assertNotNull(eventDto, "Expected a non-null EventResponseDto in the response.");

        assertEquals(validEventId, eventDto.getId(), "Event ID should match the one we requested.");
        assertEquals(VALID_EVENT_NAME, eventDto.getName(), "Name should match the event we created previously.");
        assertEquals(VALID_EVENT_DESC, eventDto.getDescription(),
                "Description should match the original event's description.");
        assertNotNull(eventDto.getStartTime(), "Expected a non-null start time.");
        assertNotNull(eventDto.getEndTime(), "Expected a non-null end time.");
    }

    @Test
    @Order(5)
    public void testGetEventByIdInvalid() {
        String url = "/events/99999";
        ResponseEntity<String> response = client.getForEntity(url, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertTrue(
                responseBody.contains("No Event found with ID: 99999"),
                "Expected error message not found in the response body.");
    }

    @Test
    @Order(6)
    public void testUpdateEventSuccess() {
        EventRequestDto requestDto = new EventRequestDto(
                UPDATED_EVENT_NAME,
                UPDATED_EVENT_DESC,
                START_TIME,
                END_TIME);
        String url = String.format("/events/%d", validEventId);

        ResponseEntity<EventResponseDto> response = client.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(requestDto),
                EventResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        EventResponseDto updatedEvent = response.getBody();
        assertNotNull(updatedEvent, "Expected a non-null EventResponseDto in the response.");

        assertEquals(validEventId, updatedEvent.getId(),
                "The event ID should remain unchanged after update.");

        assertEquals(UPDATED_EVENT_NAME, updatedEvent.getName(),
                "Event name should match the newly updated name.");
        assertEquals(UPDATED_EVENT_DESC, updatedEvent.getDescription(),
                "Event description should match the newly updated description.");

        assertNotNull(updatedEvent.getStartTime(), "Expected non-null start time.");
        assertNotNull(updatedEvent.getEndTime(), "Expected non-null end time.");
        assertEquals(START_TIME, updatedEvent.getStartTime(),
                "The start time should match what was sent in the request.");
        assertEquals(END_TIME, updatedEvent.getEndTime(),
                "The end time should match what was sent in the request.");
    }

    @Test
    @Order(7)
    public void testUpdateEventNotFound() {
        EventRequestDto requestDto = new EventRequestDto("SomeName", "Desc", START_TIME, END_TIME);
        String url = "/events/99999";
        ResponseEntity<String> response = client.exchange(
                url, HttpMethod.PUT, new HttpEntity<>(requestDto), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertTrue(
                responseBody.contains("No Event found with ID: 99999"),
                "Expected error message not found in response body.");
    }

    @Test
    @Order(8)
    public void testUpdateEventInvalidTimes() {
        Date invalidEndTime = new Date(START_TIME.getTime() - 5000);
        EventRequestDto requestDto = new EventRequestDto("Still Good", "But times fail", START_TIME, invalidEndTime);
        String url = String.format("/events/%d", validEventId);
        ResponseEntity<String> response = client.exchange(
                url, HttpMethod.PUT, new HttpEntity<>(requestDto), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertTrue(
                responseBody.contains("Event end time cannot be before the start time"),
                "Expected error message not found in response body.");
    }

    @Test
    @Order(9)
    public void testUpdateEventInvalidName() {
        EventRequestDto requestDto = new EventRequestDto("   ", "desc", START_TIME, END_TIME);
        String url = String.format("/events/%d", validEventId);
        ResponseEntity<String> response = client.exchange(
                url, HttpMethod.PUT, new HttpEntity<>(requestDto), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertTrue(
                responseBody.contains("Event name cannot be null or empty"),
                "Expected error message not found in response body.");
    }

    @Test
    @Order(10)
    public void testDeleteEventSuccess() {
        String url = String.format("/events/%d", validEventIdToDelete);
        ResponseEntity<Void> deleteResponse = client.exchange(url, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode(),
                "Expected a 200 OK when deleting an existing event.");

        ResponseEntity<String> getResponse = client.getForEntity(url, String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode(),
                "Expected 400/404 when retrieving a deleted event.");

    }

    @Test
    @Order(11)
    public void testDeleteEventNotFound() {
        String url = "/events/99999";
        ResponseEntity<String> response = client.exchange(url, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body is null");
        assertTrue(
                responseBody.contains("No Event found with ID: 99999"),
                "Expected error message for not-found event ID in response body.");
    }

    @Test
    @Order(12)
    public void testGetAllEvents() {
        ResponseEntity<EventResponseDto[]> response = client.getForEntity("/events", EventResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        EventResponseDto[] events = response.getBody();
        assertNotNull(events, "Expected non-null array of events.");
    }

    @Test
    @Order(13)
    public void testGetGamesForEventSuccess() {
        Game game = gameRepository.save(new Game("Uno", "A game"));
        scheduledGameRepository.save(new ScheduledGame(new Key(game, validEvent)));
        GameReview review = reviewRepository.save(new GameReview(3, "Comment", validPlayer, game));
        String url = String.format("/events/scheduledevent/%d", validEventId);

        ResponseEntity<GameResponseDto[]> response = client.getForEntity(url, GameResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        GameResponseDto[] games = response.getBody();
        assertNotNull(games, "Expected non-null array of GameResponseDto.");
        assertEquals(1, games.length);
        assertEquals(game.getId(), games[0].getId());
        assertEquals(review.getRating(), games[0].getRating());
    }

    @Test
    @Order(14)
    public void testGetGamesForEventNotFound() {
        String url = "/events/scheduledevent/99999";

        ResponseEntity<String> response = client.getForEntity(url, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body is null");
        assertTrue(
                responseBody.contains("No Event found with ID: 99999"),
                "Expected error message for not-found event ID in response body.");
    }

    @Test
    @Order(15)
    public void testGetScheduledEventsForGameSuccess() {
        String url = String.format("/events/scheduledgame/%d", validGameId);

        ResponseEntity<EventResponseDto[]> response = client.getForEntity(url, EventResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        EventResponseDto[] events = response.getBody();
        assertNotNull(events, "Expected a non-null array of EventResponseDto.");

    }

    @Test
    @Order(16)
    public void testGetScheduledEventsForGameNotFound() {
        String url = "/events/scheduledgame/99999";

        ResponseEntity<String> response = client.getForEntity(url, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body is null");
        assertTrue(
                responseBody.contains("Game not found with ID: 99999"),
                "Expected error message for not-found game ID in response body.");
    }

    @Test
    @Order(17)
    public void testRegisterForEventSuccess() {
        EventRequestDto newEvent = new EventRequestDto("RegEvent", "desc", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> createdResponse = client.postForEntity("/events", newEvent,
                EventResponseDto.class);

        assertEquals(HttpStatus.OK, createdResponse.getStatusCode(),
                "Expected 200 OK when creating a new event.");
        EventResponseDto createdEvent = createdResponse.getBody();
        assertNotNull(createdEvent, "Expected a non-null EventResponseDto after creation.");
        assertTrue(createdEvent.getId() > 0, "Expected a valid event ID (> 0).");

        int newEventId = createdEvent.getId();
        String registerUrl = String.format("/events/%d/player/%d", newEventId, validPlayerId);
        ResponseEntity<Void> registerResponse = client.postForEntity(registerUrl, null, Void.class);

        assertEquals(HttpStatus.OK, registerResponse.getStatusCode(),
                "Expected 200 OK when registering for the event.");

        String getPlayerEventsUrl = String.format("/events/player/%d", validPlayerId);
        ResponseEntity<EventResponseDto[]> eventsResponse = client.getForEntity(getPlayerEventsUrl,
                EventResponseDto[].class);

        assertEquals(HttpStatus.OK, eventsResponse.getStatusCode(),
                "Expected 200 OK when retrieving events for the player.");
        EventResponseDto[] playerEvents = eventsResponse.getBody();
        assertNotNull(playerEvents, "Expected a non-null array of events for the player.");

        boolean foundNewEvent = false;
        for (EventResponseDto e : playerEvents) {
            if (e.getId() == newEventId) {
                foundNewEvent = true;
                break;
            }
        }
        assertTrue(foundNewEvent,
                "Expected the newly created event to be in the player's list of registered events.");
    }

    @Test
    @Order(18)
    public void testRegisterForEventNotFound() {
        String url = String.format("/events/%d/player/%d", 99999, validPlayerId);
        ResponseEntity<String> response = client.postForEntity(url, null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body is null");
        assertTrue(
                responseBody.contains("No Event found with ID: 99999"),
                "Expected error message about not-found event in the response body.");
    }

    @Test
    @Order(19)
    public void testRegisterForEventPlayerNotFound() {
        EventRequestDto newEvent = new EventRequestDto("AnotherEvent", "desc", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> created = client.postForEntity("/events", newEvent, EventResponseDto.class);

        assertEquals(HttpStatus.OK, created.getStatusCode());

        EventResponseDto body = Objects.requireNonNull(created.getBody(), "Expected non-null body in response.");
        int newEventId = body.getId();

        String url = String.format("/events/%d/player/%d", newEventId, 99999);
        ResponseEntity<String> response = client.postForEntity(url, null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody, "Response body is null");
        assertTrue(
                responseBody.contains("Player not found with ID: 99999"),
                "Expected error message about not-found player in the response body.");
    }

    @Test
    @Order(20)
    public void testUnregisterForEventSuccess() {
        EventRequestDto newEvent = new EventRequestDto("ToUnregister", "desc", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> createdResponse = client.postForEntity("/events", newEvent,
                EventResponseDto.class);

        assertEquals(HttpStatus.OK, createdResponse.getStatusCode(),
                "Expected 200 OK when creating a new event.");
        EventResponseDto createdEvent = createdResponse.getBody();
        assertNotNull(createdEvent, "Expected a non-null EventResponseDto after creation.");
        assertTrue(createdEvent.getId() > 0, "Expected a valid event ID (> 0).");

        int newEventId = createdEvent.getId();

        String registerUrl = String.format("/events/%d/player/%d", newEventId, validPlayerId);
        ResponseEntity<Void> regResponse = client.postForEntity(registerUrl, null, Void.class);
        assertEquals(HttpStatus.OK, regResponse.getStatusCode(),
                "Expected 200 OK when registering for the event.");

        String unregisterUrl = String.format("/events/%d/player/%d", newEventId, validPlayerId);
        ResponseEntity<Void> delResponse = client.exchange(unregisterUrl, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.OK, delResponse.getStatusCode(),
                "Expected 200 OK when unregistering from the event.");

        String getPlayerEventsUrl = String.format("/events/player/%d", validPlayerId);
        ResponseEntity<EventResponseDto[]> eventsResponse = client.getForEntity(getPlayerEventsUrl,
                EventResponseDto[].class);

        assertEquals(HttpStatus.OK, eventsResponse.getStatusCode(),
                "Expected 200 OK when retrieving events for the player.");
        EventResponseDto[] playerEvents = eventsResponse.getBody();
        assertNotNull(playerEvents, "Expected a non-null array of events for the player.");

        boolean foundEvent = false;
        for (EventResponseDto e : playerEvents) {
            if (e.getId() == newEventId) {
                foundEvent = true;
                break;
            }
        }
        assertFalse(foundEvent,
                "Expected that the event would NOT be in the player's list of registered events after unregistering.");
    }

    @Test
    @Order(21)
    public void testUnregisterForEventNoEvent() {
        String url = String.format("/events/%d/player/%d", 99999, validPlayerId);
        ResponseEntity<String> response = client.exchange(url, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body, "Response body is null");
        assertTrue(
                body.contains("No Event found with ID: 99999"),
                "Expected error message about not-found event in the response body.");
    }

    @Test
    @Order(22)
    public void testUnregisterForEventPlayerNotFound() {
        EventRequestDto newEvent = new EventRequestDto("AnotherOne", "desc", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> created = client.postForEntity("/events", newEvent, EventResponseDto.class);

        assertEquals(HttpStatus.OK, created.getStatusCode());

        int newEventId = Objects.requireNonNull(created.getBody()).getId();

        String url = String.format("/events/%d/player/%d", newEventId, 99999);
        ResponseEntity<String> response = client.exchange(url, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body, "Response body is null");
        assertTrue(
                body.contains("Player not found with ID: 99999"),
                "Expected error message about not-found player in the response body.");
    }

    @Test
    @Order(23)
    public void testUnregisterForEventNoRegistration() {
        EventRequestDto newEvent = new EventRequestDto("NoRegistration", "desc", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> created = client.postForEntity("/events", newEvent, EventResponseDto.class);

        assertEquals(HttpStatus.OK, created.getStatusCode());

        EventResponseDto createdBody = Objects.requireNonNull(created.getBody(),
                "Expected a non-null EventResponseDto body");
        int newEventId = createdBody.getId();

        String url = String.format("/events/%d/player/%d", newEventId, validPlayerId);
        ResponseEntity<String> response = client.exchange(url, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body, "Response body is null");
        assertTrue(
                body.contains("No registration found for Event ID: " + newEventId),
                "Expected error message about missing registration in the response body.");
    }

    @Test
    @Order(24)
    public void testGetEventsForPlayerSuccess() {
        String url = String.format("/events/player/%d", validPlayerId);
        ResponseEntity<EventResponseDto[]> response = client.getForEntity(url, EventResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        EventResponseDto[] eventDtos = response.getBody();
        assertNotNull(eventDtos, "Expected a non-null array of EventResponseDto for the player's events.");

    }

    @Test
    @Order(25)
    public void testGetEventsForPlayerNotFound() {
        String url = "/events/player/99999";
        ResponseEntity<String> response = client.getForEntity(url, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body, "Response body is null");
        assertTrue(
                body.contains("Player not found with ID: 99999"),
                "Expected an error message about not-found player in the response body.");
    }

    @Test
    @Order(26)
    public void testGetPlayersForEventSuccess() {
        String url = String.format("/events/%d/players", validEventId);
        ResponseEntity<PlayerResponseDto[]> response = client.getForEntity(url, PlayerResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        PlayerResponseDto[] playerDtos = response.getBody();
        assertNotNull(playerDtos, "Expected a non-null array of PlayerResponseDto for this event.");

    }

    @Test
    @Order(27)
    public void testGetPlayersForEventNoEvent() {
        String url = "/events/99999/players";
        ResponseEntity<String> response = client.getForEntity(url, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body, "Response body is null");
        assertTrue(
                body.contains("No Event found with ID: 99999"),
                "Expected an error message about not-found event in the response body.");
    }
}