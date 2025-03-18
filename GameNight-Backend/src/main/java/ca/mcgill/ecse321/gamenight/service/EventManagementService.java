package ca.mcgill.ecse321.gamenight.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.Registration;
import ca.mcgill.ecse321.gamenight.model.Registration.Key;
import ca.mcgill.ecse321.gamenight.repo.EventRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.repo.RegistrationRepository;
import ca.mcgill.ecse321.gamenight.repo.ScheduledGameRepository;
import jakarta.transaction.Transactional;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.exceptions.*;

@Service
public class EventManagementService {

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public Event createEvent(String name, String description, Date startTime, Date endTime) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidEventNameException();
        }
        if (startTime != null && endTime != null && endTime.before(startTime)) {
            throw new InvalidEventTimesException();
        }
        Event newEvent = new Event(name, description, startTime, endTime);
        return eventRepository.save(newEvent);
    }

    @Transactional
    public List<Game> getGamesForEvent(int eventId) {
        getEventById(eventId);
        return scheduledGameRepository.findByKey_EventId(eventId)
                .stream()
                .map(sg -> sg.getKey().getGame())
                .collect(Collectors.toList());
    }

    @Transactional
    public Event getEventById(int eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Transactional
    public Event updateEvent(int eventId, String name, String description, Date startTime, Date endTime) {
        Event existingEvent = getEventById(eventId);
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidEventNameException();
        }
        if (name != null && !name.trim().isEmpty()) {
            existingEvent.setName(name);
        }
        if (description != null) {
            existingEvent.setDescription(description);
        }
        if (startTime != null && endTime != null && endTime.before(startTime)) {
            throw new InvalidEventTimesException();
        }
        if (startTime != null) {
            existingEvent.setStartTime(startTime);
        }
        if (endTime != null) {
            existingEvent.setEndTime(endTime);
        }
        return eventRepository.save(existingEvent);
    }

    @Transactional
    public void deleteEvent(int eventId) {
        Event event = getEventById(eventId);
        eventRepository.delete(event);
    }

    @Transactional
    public Iterable<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Autowired
    private ScheduledGameRepository scheduledGameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private GameRepository gameRepository;

    @Transactional
    public List<Event> getScheduledEventsForAGame(int gameId) {
        gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        return scheduledGameRepository.findByKey_GameId(gameId)
                .stream()
                .map(sg -> sg.getKey().getEvent())
                .collect(Collectors.toList());
    }

    @Transactional
    public void registerForEvent(int eventId, int playerId) {
        Event event = getEventById(eventId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ObjectNotFoundException("Player not found with ID: " + playerId));
        Registration registration = new Registration(new Key(player, event));
        registrationRepository.save(registration);
    }

    @Transactional
    public void unregisterForEvent(int eventId, int playerId) {
        Event event = getEventById(eventId); // Already throws EventNotFoundException
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ObjectNotFoundException("Player not found with ID: " + playerId));
        Registration existing = registrationRepository.findByKey(new Key(player, event));
        if (existing == null) {
            throw new RegistrationNotFoundException(eventId, playerId);
        }
        registrationRepository.delete(existing);
    }

    @Transactional
    public List<Event> getEventsForPlayer(int playerId) {
        playerRepository.findById(playerId)
                .orElseThrow(() -> new ObjectNotFoundException("Player not found with ID: " + playerId));
        return registrationRepository.findByKey_PlayerId(playerId)
                .stream()
                .map(r -> r.getKey().getEvent())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Player> getPlayersForEvent(int eventId) {
        getEventById(eventId);
        return registrationRepository.findByKey_EventId(eventId)
                .stream()
                .map(r -> r.getKey().getPlayer())
                .collect(Collectors.toList());
    }
}
