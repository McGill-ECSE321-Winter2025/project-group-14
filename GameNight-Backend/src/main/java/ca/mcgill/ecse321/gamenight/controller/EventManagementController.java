package ca.mcgill.ecse321.gamenight.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ca.mcgill.ecse321.gamenight.dto.EventRequestDto;
import ca.mcgill.ecse321.gamenight.dto.EventResponseDto;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.service.EventManagementService;

/**
 * REST Controller for managing Event endpoints.
 * Similar to "BorrowingManagementController".
 */
@RestController
@RequestMapping("/events") // base URL path for events
public class EventManagementController {

    @Autowired
    private EventManagementService eventService;

    /**
     * Create a new Event from the given request DTO.
     * @param requestDto The request body with event info
     * @return The newly created Event in a response DTO format
     */
    @PostMapping
    public EventResponseDto createEvent(@RequestBody EventRequestDto requestDto) {
        Event created = eventService.createEvent(
            requestDto.getName(),
            requestDto.getDescription(),
            requestDto.getStartTime(),
            requestDto.getEndTime()
        );
        return new EventResponseDto(created);
    }

    /**
     * Retrieve a single Event by its ID.
     * @param eventId ID of the event
     * @return The corresponding Event in a response DTO
     */
    @GetMapping("/{eventId}")
    public EventResponseDto getEvent(@PathVariable int eventId) {
        Event e = eventService.getEventById(eventId);
        return new EventResponseDto(e);
    }

    /**
     * Update certain fields of an existing Event.
     * @param eventId ID of the event to update
     * @param requestDto The new data to apply
     * @return The updated Event in a response DTO
     */
    @PutMapping("/{eventId}")
    public EventResponseDto updateEvent(@PathVariable int eventId, @RequestBody EventRequestDto requestDto) {
        Event updated = eventService.updateEvent(
            eventId,
            requestDto.getName(),
            requestDto.getDescription(),
            requestDto.getStartTime(),
            requestDto.getEndTime()
        );
        return new EventResponseDto(updated);
    }

    /**
     * Delete an existing Event by its ID.
     * @param eventId The ID of the event to delete
     */
    @DeleteMapping("/{eventId}")
    public void deleteEvent(@PathVariable int eventId) {
        eventService.deleteEvent(eventId);
    }

    /**
     * Get a list of all Events in the system.
     * @return A list of EventResponseDto
     */
    @GetMapping
    public List<EventResponseDto> getAllEvents() {
        return ((List<Event>) eventService.getAllEvents())
            .stream()
            .map(e -> new EventResponseDto(e))
            .collect(Collectors.toList());
    }
}

























































