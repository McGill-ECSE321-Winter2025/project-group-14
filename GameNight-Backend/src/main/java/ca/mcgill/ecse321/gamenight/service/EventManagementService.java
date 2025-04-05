package ca.mcgill.ecse321.gamenight.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.Registration;
import ca.mcgill.ecse321.gamenight.model.ScheduledGame;
import ca.mcgill.ecse321.gamenight.model.Registration.Key;
import ca.mcgill.ecse321.gamenight.repo.EventRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.repo.RegistrationRepository;
import ca.mcgill.ecse321.gamenight.repo.ScheduledGameRepository;
import jakarta.transaction.Transactional;
import ca.mcgill.ecse321.gamenight.dto.GameResponseDto;
import ca.mcgill.ecse321.gamenight.exception.*;

@Service
public class EventManagementService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private GameReviewService reviewService;

    @Autowired
    private ScheduledGameRepository scheduledGameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private GameRepository gameRepository;

    @Transactional
    public Event createEvent(String name, String description, Date startTime, Date endTime) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Event name cannot be null or empty.");
        }
        
        if (startTime != null && endTime != null && endTime.before(startTime)) {
            throw new InvalidInputException("Event end time cannot be before the start time.");
        }
        
        if (endTime != null && endTime.before(new Date())) {
            throw new InvalidInputException("Event end time cannot be before the current time.");
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
                .orElseThrow(() -> new ObjectNotFoundException("No Event found with ID: " + eventId));
    }

    @Transactional
    public Event updateEvent(int eventId, String name, String description, Date startTime, Date endTime) {
        Event existingEvent = getEventById(eventId);

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Event name cannot be null or empty.");
        }
        existingEvent.setName(name);

        if (description != null) {
            existingEvent.setDescription(description);
        }

        if (startTime != null && endTime != null && endTime.before(startTime)) {
            throw new InvalidInputException("Event end time cannot be before the start time.");
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
    
        List<ScheduledGame> scheduled = scheduledGameRepository.findByKey_EventId(eventId);
        for (ScheduledGame sg : scheduled) {
            scheduledGameRepository.delete(sg);
        }
    
        List<Registration> regs = registrationRepository.findByKey_EventId(eventId);
        for (Registration r : regs) {
            registrationRepository.delete(r);
        }
    
        eventRepository.delete(event);
    }
    
    

    @Transactional
    public Iterable<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional
    public List<Event> getScheduledEventsForAGame(int gameId) {
        gameRepository.findById(gameId)
                .orElseThrow(() -> new ObjectNotFoundException("Game not found with ID: " + gameId));

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
        Event event = getEventById(eventId);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ObjectNotFoundException("Player not found with ID: " + playerId));

        Registration existing = registrationRepository.findByKey(new Key(player, event));
        if (existing == null) {
            throw new ObjectNotFoundException(
                    "No registration found for Event ID: " + eventId + " and Player ID: " + playerId);
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

    @Transactional
    public List<Event> getEventsCreatedByPlayer(int playerId) {
        playerRepository.findById(playerId)
                .orElseThrow(() -> new ObjectNotFoundException("Player not found with ID: " + playerId));

        List<Event> allEvents = (List<Event>) eventRepository.findAll();

        List<Event> createdByPlayer = new ArrayList<>();

        for (Event event : allEvents) {
            Registration earliestReg = registrationRepository.findFirstByKey_EventIdOrderByCreatedAtAsc(event.getId());
            if (earliestReg != null && earliestReg.getKey().getPlayer().getId() == playerId) {
                createdByPlayer.add(event);
            }
        }

        return createdByPlayer;
    }



    @Transactional
    public void scheduleGamesForEvent(int eventId, List<Integer> gameIds) {
        Event event = getEventById(eventId);
    
        if (gameIds == null || gameIds.isEmpty()) {
            throw new InvalidInputException("Cannot schedule an empty list of games.");
        }
    
        for (Integer gameId : gameIds) {
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new ObjectNotFoundException("Game not found with ID: " + gameId));
    
            ScheduledGame.Key key = new ScheduledGame.Key(game, event);
    
            ScheduledGame scheduledGame = new ScheduledGame(key);
            scheduledGameRepository.save(scheduledGame);
        }
    }
    
    

}
