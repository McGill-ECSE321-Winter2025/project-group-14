package ca.mcgill.ecse321.gamenight.service;

import java.util.Date;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.repo.EventRepository;
import jakarta.transaction.Transactional;

/**
 * Service layer for managing Events.
 * <p>
 * Provides methods to create, retrieve, update,
 * and delete events from the database. Additional
 * logic or constraints can be added as needed.
 */
@Service
public class EventManagementService {

    @Autowired
    private EventRepository eventRepository;

    /**
     * Creates and saves a new Event.
     *
     * @param name        the name of the event (required, non-empty)
     * @param description an optional description
     * @param startTime   event start time
     * @param endTime     event end time
     * @return the created and saved Event
     * @throws IllegalArgumentException if name is invalid or endTime < startTime
     */
    @Transactional
    public Event createEvent(String name, String description, Date startTime, Date endTime) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be null or empty.");
        }
        if (startTime != null && endTime != null && endTime.before(startTime)) {
            throw new IllegalArgumentException("Event end time cannot be before the start time.");
        }

        Event newEvent = new Event(name, description, startTime, endTime);
        return eventRepository.save(newEvent);
    }

    /**
     * Retrieves an Event by its unique ID.
     *
     * @param eventId the unique integer ID of the event
     * @return the corresponding Event
     * @throws NoSuchElementException if no Event exists with the given ID
     */
    @Transactional
    public Event getEventById(int eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("No Event found with ID: " + eventId));
    }

    /**
     * Updates an existing Event with new details.
     * Only the non-null fields are updated.
     *
     * @param eventId     the ID of the Event to update
     * @param name        the new name (optional)
     * @param description the new description (optional)
     * @param startTime   the new start time (optional)
     * @param endTime     the new end time (optional)
     * @return the updated Event
     * @throws NoSuchElementException   if the Event does not exist
     * @throws IllegalArgumentException if endTime < startTime
     */
    @Transactional
    public Event updateEvent(int eventId, String name, String description, Date startTime, Date endTime) {
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

        return eventRepository.save(existingEvent);
    }

    /**
     * Deletes an existing Event by its unique ID.
     *
     * @param eventId the ID of the Event to delete
     * @throws NoSuchElementException if the Event does not exist
     */
    @Transactional
    public void deleteEvent(int eventId) {
        Event event = getEventById(eventId);
        eventRepository.delete(event);
    }

    /**
     * Retrieves all Events from the system.
     *
     * @return an Iterable of Event objects (may be empty if none exist)
     */
    @Transactional
    public Iterable<Event> getAllEvents() {
        return eventRepository.findAll();
    }

}
