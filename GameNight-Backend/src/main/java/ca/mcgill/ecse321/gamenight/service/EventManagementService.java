package ca.mcgill.ecse321.gamenight.service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.Registration;
import ca.mcgill.ecse321.gamenight.model.ScheduledGame;
import ca.mcgill.ecse321.gamenight.repo.EventRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.repo.RegistrationRepository;
import ca.mcgill.ecse321.gamenight.repo.ScheduledGameRepository;
import jakarta.transaction.Transactional;

@Service
public class EventManagementService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ScheduledGameRepository scheduledGameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Transactional
    public Event createEvent(String name, String description, Date startTime, Date endTime, int maxParticipants) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be null or empty.");
        }
        if (startTime != null && endTime != null && endTime.before(startTime)) {
            throw new IllegalArgumentException("Event end time cannot be before the start time.");
        }

        Event newEvent = new Event(name, description, startTime, endTime);
        newEvent.setMaxParticipants(maxParticipants);
        return eventRepository.save(newEvent);
    }

    @Transactional
    public Event getEventById(int eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("No Event found with ID: " + eventId));
    }

    @Transactional
    public Event updateEvent(int eventId, String name, String description, Date startTime, Date endTime, Integer maxParticipants) {
        Event existingEvent = getEventById(eventId);

        if (name != null && !name.trim().isEmpty()) {
            existingEvent.setName(name);
        }
        if (description != null) {
            existingEvent.setDescription(description);
        }
        if (startTime != null && endTime != null && endTime.before(startTime)) {
            throw new IllegalArgumentException("Event end time cannot be before the start time.");
        }
        if (startTime != null) {
            existingEvent.setStartTime(startTime);
        }
        if (endTime != null) {
            existingEvent.setEndTime(endTime);
        }
        if (maxParticipants != null) {
            existingEvent.setMaxParticipants(maxParticipants);
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

    @Transactional
    public List<ScheduledGame> getScheduledEventsForAGame(int gameId) {
        return scheduledGameRepository.findByGameId(gameId);
    }

    @Transactional
    public boolean isEventFull(int eventId) {
        Event event = getEventById(eventId);
        long registeredCount = registrationRepository.countByEvent(event);
        return registeredCount >= event.getMaxParticipants();
    }

    @Transactional
    public void registerForEvent(int eventId, int playerId) {
        Event event = getEventById(eventId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoSuchElementException("No Player found with ID: " + playerId));

        if (isEventFull(eventId)) {
            throw new IllegalStateException("Cannot register: Event is full.");
        }

        Registration registration = new Registration(player, event);
        registrationRepository.save(registration);
    }

    @Transactional
    public void unregisterForEvent(int eventId, int playerId) {
        Event event = getEventById(eventId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoSuchElementException("No Player found with ID: " + playerId));

        Registration registration = registrationRepository.findByEventAndPlayer(event, player)
                .orElseThrow(() -> new NoSuchElementException("No registration found for this player and event"));

        registrationRepository.delete(registration);
    }

    @Transactional
    public List<Event> getEventsForPlayer(int playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoSuchElementException("No Player found with ID: " + playerId));
        return registrationRepository.findByPlayer(player)
                .stream()
                .map(Registration::getEvent)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Player> getPlayersForEvent(int eventId) {
        Event event = getEventById(eventId);
        return registrationRepository.findByEvent(event)
                .stream()
                .map(Registration::getPlayer)
                .collect(Collectors.toList());
    }
}
