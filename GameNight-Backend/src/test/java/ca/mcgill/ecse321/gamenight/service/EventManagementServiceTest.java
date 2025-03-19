package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.Registration;
import ca.mcgill.ecse321.gamenight.model.Registration.Key;
import ca.mcgill.ecse321.gamenight.model.ScheduledGame;
import ca.mcgill.ecse321.gamenight.repo.EventRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.repo.RegistrationRepository;
import ca.mcgill.ecse321.gamenight.repo.ScheduledGameRepository;
import ca.mcgill.ecse321.gamenight.exception.*;

class EventManagementServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ScheduledGameRepository scheduledGameRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private EventManagementService eventService;

    private String name;
    private String desc;
    private Date start;
    private Date end;
    private Date past;
    private Event validEvent;
    private Event nightEvent;
    private Event eventOne;
    private Event updateEvent;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        name = "Board Game Night";
        desc = "Evening of fun games";
        start = new Date();
        end = new Date(start.getTime() + 3600000);
        past = new Date(start.getTime() - 10000);

        validEvent = new Event(name, desc, start, end);
        nightEvent = new Event("Night", "desc", new Date(), new Date());
        nightEvent.setName("TestEvent");
        eventOne = new Event("Event", "desc", new Date(), new Date());
        eventOne.setName("EventOne");

        updateEvent = new Event("Old Name", "Old Desc", new Date(), new Date());

    }

    @Test
    void testCreateEventSuccess() {
        when(eventRepository.save(any(Event.class))).thenReturn(validEvent);
        Event result = eventService.createEvent(name, desc, start, end);
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(desc, result.getDescription());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCreateEventInvalidName() {
        assertThrows(InvalidInputException.class, () -> {
            eventService.createEvent(null, "desc", start, end);
        });
    }

    @Test
    void testCreateEventEndBeforeStart() {
        assertThrows(InvalidInputException.class, () -> {
            eventService.createEvent("Game Night", "desc", start, past);
        });
    }

    @Test
    void testCreateEventWithNullEndTime() {
        when(eventRepository.save(any(Event.class))).thenReturn(validEvent);

        Event result = eventService.createEvent("Test Name", "Test Desc", start, null);

        assertNotNull(result);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCreateEventWithNullStartTime() {
        when(eventRepository.save(any(Event.class))).thenReturn(validEvent);

        Event result = eventService.createEvent("Test Name", "Test Desc", null, end);

        assertNotNull(result);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCreateEventWithNullTimes() {
        when(eventRepository.save(any(Event.class))).thenReturn(validEvent);

        Event result = eventService.createEvent("Test Name", "Test Desc", null, null);

        assertNotNull(result);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testGetEventByIdSuccess() {
        when(eventRepository.findById(1)).thenReturn(Optional.of(nightEvent));
        Event result = eventService.getEventById(1);
        assertNotNull(result);
        assertEquals("TestEvent", result.getName());
        verify(eventRepository, times(1)).findById(1);
    }

    @Test
    void testGetEventByIdNotFound() {
        when(eventRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.getEventById(99);
        });
    }

    @Test
    void testDeleteEventSuccess() {
        when(eventRepository.findById(5)).thenReturn(Optional.of(nightEvent));
        eventService.deleteEvent(5);
        verify(eventRepository, times(1)).delete(nightEvent);
    }

    @Test
    void testDeleteEventNotFound() {
        when(eventRepository.findById(55)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.deleteEvent(55);
        });
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void testRegisterForEvent() {
        when(eventRepository.findById(1)).thenReturn(Optional.of(eventOne));
        Player p = mock(Player.class);
        when(playerRepository.findById(2)).thenReturn(Optional.of(p));
        eventService.registerForEvent(1, 2);
        verify(registrationRepository, times(1)).save(any(Registration.class));
    }

    @Test
    void testRegisterForEventNotFound() {
        when(eventRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.registerForEvent(1, 2);
        });
    }

    @Test
    void testRegisterForEventPlayerNotFound() {
        when(eventRepository.findById(1)).thenReturn(Optional.of(eventOne));
        when(playerRepository.findById(2)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.registerForEvent(1, 2);
        });
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void testGetGamesForEvent() {
        when(eventRepository.findById(10)).thenReturn(Optional.of(eventOne));
        Game g1 = mock(Game.class);
        Game g2 = mock(Game.class);
        when(scheduledGameRepository.findByKey_EventId(10)).thenReturn(List.of(
                new ca.mcgill.ecse321.gamenight.model.ScheduledGame(
                        new ca.mcgill.ecse321.gamenight.model.ScheduledGame.Key(g1, eventOne)),
                new ca.mcgill.ecse321.gamenight.model.ScheduledGame(
                        new ca.mcgill.ecse321.gamenight.model.ScheduledGame.Key(g2, eventOne))));
        List<Game> result = eventService.getGamesForEvent(10);
        assertEquals(2, result.size());
    }

    @Test
    void testGetGamesForEventEventNotFound() {
        when(eventRepository.findById(999)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.getGamesForEvent(999);
        });
        verify(scheduledGameRepository, never()).findByKey_EventId(anyInt());
    }

    @Test
    void testUpdateEventSuccess() {
        when(eventRepository.findById(100)).thenReturn(Optional.of(updateEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));
        Event updated = eventService.updateEvent(100, "New Name", "New Desc", start, end);
        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals("New Desc", updated.getDescription());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testUpdateEventNotFound() {
        when(eventRepository.findById(200)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.updateEvent(200, "Name", "Desc", start, end);
        });
    }

    @Test
    void testUpdateEventInvalidTimes() {
        when(eventRepository.findById(300)).thenReturn(Optional.of(updateEvent));
        assertThrows(InvalidInputException.class, () -> {
            eventService.updateEvent(300, "AnotherName", "AnotherDesc", start, past);
        });
    }

    @Test
    void testUpdateEventInvalidName() {
        when(eventRepository.findById(100)).thenReturn(Optional.of(updateEvent));
        assertThrows(InvalidInputException.class, () -> {
            eventService.updateEvent(100, "   ", "New Description", start, end);
        });
    }

    @Test
    void testUpdateEventNullDescription() {
        Date oldStart = new Date();
        Date oldEnd = new Date(oldStart.getTime() + 3600000);
        Event existing = new Event("Old Name", "Old Desc", oldStart, oldEnd);

        when(eventRepository.findById(500)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event updated = eventService.updateEvent(500, "New Name", null, oldStart, oldEnd);

        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals("Old Desc", updated.getDescription());
        assertEquals(oldStart, updated.getStartTime());
        assertEquals(oldEnd, updated.getEndTime());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testUpdateEventNullStartTime() {
        Date oldStart = new Date();
        Date oldEnd = new Date(oldStart.getTime() + 3600000);
        Event existing = new Event("Old Name", "Old Desc", oldStart, oldEnd);

        when(eventRepository.findById(501)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Date newEnd = new Date(oldStart.getTime() + 7200000);
        Event updated = eventService.updateEvent(501, "Name Stays", "Desc Stays", null, newEnd);

        assertNotNull(updated);
        assertEquals(oldStart, updated.getStartTime());
        assertEquals(newEnd, updated.getEndTime());
        assertEquals("Name Stays", updated.getName());
        assertEquals("Desc Stays", updated.getDescription());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testUpdateEventNullEndTime() {
        Date oldStart = new Date();
        Date oldEnd = new Date(oldStart.getTime() + 3600000);
        Event existing = new Event("Old Name", "Old Desc", oldStart, oldEnd);

        when(eventRepository.findById(502)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Date newStart = new Date(oldStart.getTime() + 10000);
        Event updated = eventService.updateEvent(502, "Any Name", "Any Desc", newStart, null);

        assertNotNull(updated);
        assertEquals(oldEnd, updated.getEndTime());
        assertEquals(newStart, updated.getStartTime());
        assertEquals("Any Name", updated.getName());
        assertEquals("Any Desc", updated.getDescription());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testGetAllEventsSuccess() {
        Event e1 = new Event("E1", "desc1", new Date(), new Date());
        Event e2 = new Event("E2", "desc2", new Date(), new Date());
        List<Event> events = List.of(e1, e2);
        when(eventRepository.findAll()).thenReturn(events);
        Iterable<Event> result = eventService.getAllEvents();
        assertNotNull(result);
        assertEquals(2, ((List<Event>) result).size());
    }

    @Test
    void testUpdateEventNullName() {
        when(eventRepository.findById(600)).thenReturn(Optional.of(updateEvent));

        assertThrows(InvalidInputException.class, () -> {
            eventService.updateEvent(600, null, "Some Description", start, end);
        });

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testGetScheduledEventsForAGameSuccess() {
        Game someGame = new Game("Some Game", "Description");
        when(gameRepository.findById(50)).thenReturn(Optional.of(someGame));

        Event e1 = new Event("A", "desc", new Date(), new Date());
        Event e2 = new Event("B", "desc", new Date(), new Date());
        Game mockGame = mock(Game.class);
        when(scheduledGameRepository.findByKey_GameId(50)).thenReturn(List.of(
                new ScheduledGame(new ScheduledGame.Key(mockGame, e1)),
                new ScheduledGame(new ScheduledGame.Key(mockGame, e2))));

        List<Event> scheduled = eventService.getScheduledEventsForAGame(50);
        assertEquals(2, scheduled.size());
    }

    @Test
    void testGetScheduledEventsForAGameGameNotFound() {
        when(gameRepository.findById(60)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.getScheduledEventsForAGame(60);
        });
        verify(scheduledGameRepository, never()).findByKey_GameId(anyInt());
    }

    @Test
    void testUnregisterForEventSuccess() {
        Event e = new Event("Event", "desc", new Date(), new Date());
        Player p = new Player();
        Registration reg = new Registration(new Key(p, e));
        when(eventRepository.findById(5)).thenReturn(Optional.of(e));
        when(playerRepository.findById(6)).thenReturn(Optional.of(p));
        when(registrationRepository.findByKey(any(Key.class))).thenReturn(reg);
        eventService.unregisterForEvent(5, 6);
        verify(registrationRepository, times(1)).delete(reg);
    }

    @Test
    void testUnregisterForEventNoEvent() {
        when(eventRepository.findById(5)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.unregisterForEvent(5, 6);
        });
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void testUnregisterForEventNoRegistration() {
        Event e = new Event("Event", "desc", new Date(), new Date());
        Player p = new Player();
        when(eventRepository.findById(7)).thenReturn(Optional.of(e));
        when(playerRepository.findById(8)).thenReturn(Optional.of(p));
        when(registrationRepository.findByKey(any(Key.class))).thenReturn(null);

        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.unregisterForEvent(7, 8);
        });
    }

    @Test
    void testUnregisterForEventPlayerNotFound() {
        when(eventRepository.findById(10)).thenReturn(Optional.of(eventOne));
        when(playerRepository.findById(100)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.unregisterForEvent(10, 100);
        });
        verify(registrationRepository, never()).findByKey(any(Key.class));
        verify(registrationRepository, never()).delete(any());
    }

    @Test
    void testGetEventsForPlayerSuccess() {
        Player p = new Player();
        when(playerRepository.findById(20)).thenReturn(Optional.of(p));
        Event e1 = new Event("E1", "desc1", new Date(), new Date());
        Event e2 = new Event("E2", "desc2", new Date(), new Date());
        Registration r1 = new Registration(new Key(p, e1));
        Registration r2 = new Registration(new Key(p, e2));
        when(registrationRepository.findByKey_PlayerId(20)).thenReturn(List.of(r1, r2));
        List<Event> events = eventService.getEventsForPlayer(20);
        assertEquals(2, events.size());
    }

    @Test
    void testGetEventsForPlayerNotFound() {
        when(playerRepository.findById(30)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.getEventsForPlayer(30);
        });
    }

    @Test
    void testGetPlayersForEventSuccess() {
        Event e = new Event("E", "desc", new Date(), new Date());
        when(eventRepository.findById(90)).thenReturn(Optional.of(e));
        Player p1 = new Player();
        Player p2 = new Player();
        Registration r1 = new Registration(new Key(p1, e));
        Registration r2 = new Registration(new Key(p2, e));
        when(registrationRepository.findByKey_EventId(90)).thenReturn(List.of(r1, r2));
        List<Player> players = eventService.getPlayersForEvent(90);
        assertEquals(2, players.size());
    }

    @Test
    void testGetPlayersForEventNoEvent() {
        when(eventRepository.findById(95)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> {
            eventService.getPlayersForEvent(95);
        });
    }

}
