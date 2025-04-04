package ca.mcgill.ecse321.gamenight.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ca.mcgill.ecse321.gamenight.dto.EventRequestDto;
import ca.mcgill.ecse321.gamenight.dto.EventResponseDto;
import ca.mcgill.ecse321.gamenight.dto.GameResponseDto;
import ca.mcgill.ecse321.gamenight.dto.PlayerResponseDto;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.ScheduledGame;
import ca.mcgill.ecse321.gamenight.service.EventManagementService;
import ca.mcgill.ecse321.gamenight.service.GameReviewService;

/**
 * REST controller for managing events
 * 
 * This controller handles endpoints related to creating, accessing, updating,
 * and deleting events, as well as retrieving scheduled games and managing
 * player registrations for events.
 */
@RestController
@RequestMapping("/events")
public class EventManagementController {

    @Autowired
    private EventManagementService eventService;

    @Autowired
    private GameReviewService reviewService;

    /**
     * Create a new event
     * 
     * @param requestDto The request body containing the new event data
     * @return The created event
     */
    @PostMapping
    public EventResponseDto createEvent(@RequestBody EventRequestDto requestDto) {
        Event created = eventService.createEvent(
                requestDto.getName(),
                requestDto.getDescription(),
                requestDto.getStartTime(),
                requestDto.getEndTime());
        return new EventResponseDto(created);
    }

    /**
     * Return the event with the given ID
     * 
     * @param eventId The primary key of the event to find
     * @return The event with the given ID
     */
    @GetMapping("/{eventId}")
    public EventResponseDto getEvent(@PathVariable int eventId) {
        Event e = eventService.getEventById(eventId);
        return new EventResponseDto(e);
    }

    /**
     * Update the event with the given ID
     * 
     * @param eventId    The primary key of the event to update
     * @param requestDto The updated event information
     * @return The updated event
     */
    @PutMapping("/{eventId}")
    public EventResponseDto updateEvent(@PathVariable int eventId, @RequestBody EventRequestDto requestDto) {
        Event updated = eventService.updateEvent(
                eventId,
                requestDto.getName(),
                requestDto.getDescription(),
                requestDto.getStartTime(),
                requestDto.getEndTime());
        return new EventResponseDto(updated);
    }

    /**
     * Delete the event with the given ID
     * 
     * @param eventId The primary key of the event to delete
     */
    @DeleteMapping("/{eventId}")
    public void deleteEvent(@PathVariable int eventId) {
        eventService.deleteEvent(eventId);
    }

    /**
     * Return all events in the system
     * 
     * @return A list of all events
     */
    @GetMapping
    public List<EventResponseDto> getAllEvents() {
        return ((List<Event>) eventService.getAllEvents())
                .stream()
                .map(EventResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Return all games scheduled for the event with the given ID
     * 
     * @param eventId The primary key of the event
     * @return A list of games for the given event
     */
    @GetMapping("/scheduledevent/{eventId}")
    public List<GameResponseDto> getGamesForEvent(@PathVariable int eventId) {
        List<Game> games = eventService.getGamesForEvent(eventId);
        ArrayList<GameResponseDto> response = new ArrayList<>();
        for (Game game: games) {
            Double rating = reviewService.getAverageRatingForGame(game);
            response.add(new GameResponseDto(game, rating));
        }
        return response;
    }

    /**
     * Return all events scheduled for the game with the given ID
     * 
     * @param gameId The primary key of the game
     * @return A list of events scheduled for the given game
     */
    @GetMapping("/scheduledgame/{gameId}")
    public List<EventResponseDto> getScheduledEventsForAGame(@PathVariable int gameId) {
        return eventService.getScheduledEventsForAGame(gameId)
                .stream()
                .map(EventResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Register the player with the given ID for the event with the given ID
     * 
     * @param eventId  The primary key of the event
     * @param playerId The primary key of the player
     */
    @PostMapping("/{eventId}/player/{playerId}")
    public void registerForEvent(@PathVariable int eventId, @PathVariable int playerId) {
        eventService.registerForEvent(eventId, playerId);
    }

    /**
     * Unregister the player with the given ID from the event with the given ID
     * 
     * @param eventId  The primary key of the event
     * @param playerId The primary key of the player
     */
    @DeleteMapping("/{eventId}/player/{playerId}")
    public void unregisterForEvent(@PathVariable int eventId, @PathVariable int playerId) {
        eventService.unregisterForEvent(eventId, playerId);
    }

    /**
     * Return all events that the player with the given ID is registered for
     * 
     * @param playerId The primary key of the player
     * @return A list of events that the player is registered for
     */
    @GetMapping("/player/{playerId}")
    public List<EventResponseDto> getEventsForPlayer(@PathVariable int playerId) {
        return eventService.getEventsForPlayer(playerId)
                .stream()
                .map(EventResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Return all players registered for the event with the given ID
     * 
     * @param eventId The primary key of the event
     * @return A list of players for the given event
     */
    @GetMapping("/{eventId}/players")
    public List<PlayerResponseDto> getPlayersForEvent(@PathVariable int eventId) {
        return eventService.getPlayersForEvent(eventId)
                .stream()
                .map(PlayerResponseDto::new)
                .collect(Collectors.toList());
    }

}
