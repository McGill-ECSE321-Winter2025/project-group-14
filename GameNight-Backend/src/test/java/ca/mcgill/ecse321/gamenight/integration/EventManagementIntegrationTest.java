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