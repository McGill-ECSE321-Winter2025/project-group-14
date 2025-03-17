package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

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
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
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
    private GameOwnerRepository gameOwnerRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private ScheduledGameRepository scheduledGameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private int validEventId;
    private int validPlayerId;
    private int validGameId;
    private int validEventIdToDelete;

    private static final String VALID_EVENT_NAME = "Board Game Night";
    private static final String VALID_EVENT_DESC = "Playing all sorts of board games";
    private static final String UPDATED_EVENT_NAME = "Updated Night";
    private static final String UPDATED_EVENT_DESC = "Updated Description";
    private static final Date START_TIME = new Date(System.currentTimeMillis() + 3600000); 
    private static final Date END_TIME   = new Date(System.currentTimeMillis() + 7200000);

    @BeforeAll
    public void setupDatabase() {
        registrationRepository.deleteAll();
        scheduledGameRepository.deleteAll();
    
        eventRepository.deleteAll();
        
        playerRepository.deleteAll();
        gameOwnerRepository.deleteAll();
        gameRepository.deleteAll();
        personRepository.deleteAll();

        Person playerPerson = personRepository.save(new Person("player@email.com", "pass123", "PlayerOne"));
        Player player = playerRepository.save(new Player(playerPerson));
        validPlayerId = player.getId();

        Person ownerPerson = personRepository.save(new Person("owner@email.com", "pass456", "OwnerOne"));
        GameOwner owner = gameOwnerRepository.save(new GameOwner(ownerPerson));

        Game newGame = new Game("SomeGame", "A test game");
        gameRepository.save(newGame);
        validGameId = newGame.getId();

        Event eventToGet = new Event(VALID_EVENT_NAME, VALID_EVENT_DESC, START_TIME, END_TIME);
        eventToGet = eventRepository.save(eventToGet);
        validEventId = eventToGet.getId();

        Event eventToDelete = new Event("ToDelete", "This will be deleted", START_TIME, END_TIME);
        eventToDelete = eventRepository.save(eventToDelete);
        validEventIdToDelete = eventToDelete.getId();
    }

@AfterAll
public void clearDatabase() {
    registrationRepository.deleteAll();
    scheduledGameRepository.deleteAll();

    eventRepository.deleteAll();

    playerRepository.deleteAll();
    gameOwnerRepository.deleteAll();
    gameRepository.deleteAll();
    personRepository.deleteAll();
}


    @Test
    @Order(1)
    public void testCreateValidEvent() {
        EventRequestDto requestDto = new EventRequestDto("New Event", "Description", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> response =
                client.postForEntity("/events", requestDto, EventResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(2)
    public void testCreateEventInvalidName() {
        EventRequestDto requestDto = new EventRequestDto("", "desc", START_TIME, END_TIME);
        ResponseEntity<String> response =
                client.postForEntity("/events", requestDto, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(3)
    public void testCreateEventInvalidTimes() {
        Date invalidEndTime = new Date(START_TIME.getTime() - 10000);
        EventRequestDto requestDto = new EventRequestDto("TimeFail", "desc", START_TIME, invalidEndTime);
        ResponseEntity<String> response =
                client.postForEntity("/events", requestDto, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    @Order(4)
    public void testGetEventByIdValid() {
        String url = String.format("/events/%d", validEventId);
        ResponseEntity<EventResponseDto> response =
                client.getForEntity(url, EventResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(5)
    public void testGetEventByIdInvalid() {
        String url = "/events/99999";
        ResponseEntity<String> response =
                client.getForEntity(url, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    @Order(6)
    public void testUpdateEventSuccess() {
        EventRequestDto requestDto = new EventRequestDto(UPDATED_EVENT_NAME, UPDATED_EVENT_DESC, START_TIME, END_TIME);
        String url = String.format("/events/%d", validEventId);
        ResponseEntity<EventResponseDto> response = client.exchange(
                url, HttpMethod.PUT, new HttpEntity<>(requestDto), EventResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void testUpdateEventNotFound() {
        EventRequestDto requestDto = new EventRequestDto("SomeName", "Desc", START_TIME, END_TIME);
        String url = "/events/99999";
        ResponseEntity<String> response = client.exchange(
                url, HttpMethod.PUT, new HttpEntity<>(requestDto), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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
    }

    @Test
    @Order(9)
    public void testUpdateEventInvalidName() {
        EventRequestDto requestDto = new EventRequestDto("   ", "desc", START_TIME, END_TIME);
        String url = String.format("/events/%d", validEventId);
        ResponseEntity<String> response = client.exchange(
                url, HttpMethod.PUT, new HttpEntity<>(requestDto), String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    @Order(10)
    public void testDeleteEventSuccess() {
        String url = String.format("/events/%d", validEventIdToDelete);
        ResponseEntity<Void> response = client.exchange(url, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(11)
    public void testDeleteEventNotFound() {
        String url = "/events/99999";
        ResponseEntity<String> response = client.exchange(url, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    @Order(12)
    public void testGetAllEvents() {
        ResponseEntity<EventResponseDto[]> response =
                client.getForEntity("/events", EventResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }


@Test
@Order(13)
public void testGetGamesForEventSuccess() {
    String url = String.format("/events/scheduledevent/%d", validEventId);
    
    ResponseEntity<GameResponseDto[]> response = client.getForEntity(url, GameResponseDto[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

}

@Test
@Order(14)
public void testGetGamesForEventNotFound() {
    String url = "/events/scheduledevent/99999";
    
    ResponseEntity<String> response = client.getForEntity(url, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
}


@Test
@Order(15)
public void testGetScheduledEventsForGameSuccess() {
    String url = String.format("/events/scheduledgame/%d", validGameId);
    
    ResponseEntity<EventResponseDto[]> response = client.getForEntity(url, EventResponseDto[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

}

@Test
@Order(16)
public void testGetScheduledEventsForGameNotFound() {
    String url = "/events/scheduledgame/99999";

    ResponseEntity<String> response = client.getForEntity(url, String.class);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
}



    @Test
    @Order(17)
    public void testRegisterForEventSuccess() {
        EventRequestDto newEvent = new EventRequestDto("RegEvent", "desc", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> created =
                client.postForEntity("/events", newEvent, EventResponseDto.class);

        assertEquals(HttpStatus.OK, created.getStatusCode());
        int newEventId = (created.getBody() != null) ? created.getBody().getId() : -1;

        String url = String.format("/events/%d/player/%d", newEventId, validPlayerId);
        ResponseEntity<Void> response = client.postForEntity(url, null, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(18)
    public void testRegisterForEventNotFound() {
        String url = String.format("/events/%d/player/%d", 99999, validPlayerId);
        ResponseEntity<String> response = client.postForEntity(url, null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(19)
    public void testRegisterForEventPlayerNotFound() {
        EventRequestDto newEvent = new EventRequestDto("AnotherEvent", "desc", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> created =
                client.postForEntity("/events", newEvent, EventResponseDto.class);

        assertEquals(HttpStatus.OK, created.getStatusCode());
        int newEventId = (created.getBody() != null) ? created.getBody().getId() : -1;

        String url = String.format("/events/%d/player/%d", newEventId, 99999);
        ResponseEntity<String> response =
                client.postForEntity(url, null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    @Order(20)
    public void testUnregisterForEventSuccess() {
        EventRequestDto newEvent = new EventRequestDto("ToUnregister", "desc", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> created =
                client.postForEntity("/events", newEvent, EventResponseDto.class);

        assertEquals(HttpStatus.OK, created.getStatusCode());
        int newEventId = (created.getBody() != null) ? created.getBody().getId() : -1;

        String registerUrl = String.format("/events/%d/player/%d", newEventId, validPlayerId);
        ResponseEntity<Void> regResponse = client.postForEntity(registerUrl, null, Void.class);
        assertEquals(HttpStatus.OK, regResponse.getStatusCode());

        String unregisterUrl = String.format("/events/%d/player/%d", newEventId, validPlayerId);
        ResponseEntity<Void> delResponse = client.exchange(unregisterUrl, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.OK, delResponse.getStatusCode());
    }

    @Test
    @Order(21)
    public void testUnregisterForEventNoEvent() {
        String url = String.format("/events/%d/player/%d", 99999, validPlayerId);
        ResponseEntity<String> response =
                client.exchange(url, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(22)
    public void testUnregisterForEventPlayerNotFound() {
        // create new event
        EventRequestDto newEvent = new EventRequestDto("AnotherOne", "desc", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> created =
                client.postForEntity("/events", newEvent, EventResponseDto.class);

        assertEquals(HttpStatus.OK, created.getStatusCode());
        int newEventId = (created.getBody() != null) ? created.getBody().getId() : -1;

        String url = String.format("/events/%d/player/%d", newEventId, 99999);
        ResponseEntity<String> response =
                client.exchange(url, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(23)
    public void testUnregisterForEventNoRegistration() {
        // create new event
        EventRequestDto newEvent = new EventRequestDto("NoRegistration", "desc", START_TIME, END_TIME);
        ResponseEntity<EventResponseDto> created =
                client.postForEntity("/events", newEvent, EventResponseDto.class);

        assertEquals(HttpStatus.OK, created.getStatusCode());
        int newEventId = (created.getBody() != null) ? created.getBody().getId() : -1;

        // attempt to unregister without registering first
        String url = String.format("/events/%d/player/%d", newEventId, validPlayerId);
        ResponseEntity<String> response =
                client.exchange(url, HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    @Order(24)
    public void testGetEventsForPlayerSuccess() {
        String url = String.format("/events/player/%d", validPlayerId);
        ResponseEntity<EventResponseDto[]> response =
                client.getForEntity(url, EventResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(25)
    public void testGetEventsForPlayerNotFound() {
        String url = "/events/player/99999";
        ResponseEntity<String> response =
                client.getForEntity(url, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    @Order(26)
    public void testGetPlayersForEventSuccess() {
        String url = String.format("/events/%d/players", validEventId);
        ResponseEntity<PlayerResponseDto[]> response =
                client.getForEntity(url, PlayerResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(27)
    public void testGetPlayersForEventNoEvent() {
        String url = "/events/99999/players";
        ResponseEntity<String> response =
                client.getForEntity(url, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}