package ca.mcgill.ecse321.gamenight.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ca.mcgill.ecse321.gamenight.dto.EventRequestDto;
import ca.mcgill.ecse321.gamenight.dto.EventResponseDto;
import ca.mcgill.ecse321.gamenight.dto.GameResponseDto;
import ca.mcgill.ecse321.gamenight.dto.PlayerResponseDto;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.service.EventManagementService;

@RestController
@RequestMapping("/events")
public class EventManagementController {

    @Autowired
    private EventManagementService eventService;

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

    @GetMapping("/{eventId}")
    public EventResponseDto getEvent(@PathVariable int eventId) {
        Event e = eventService.getEventById(eventId);
        return new EventResponseDto(e);
    }

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

    @DeleteMapping("/{eventId}")
    public void deleteEvent(@PathVariable int eventId) {
        eventService.deleteEvent(eventId);
    }

    @GetMapping
    public List<EventResponseDto> getAllEvents() {
        return ((List<Event>) eventService.getAllEvents())
            .stream()
            .map(EventResponseDto::new)
            .collect(Collectors.toList());
    }
    @GetMapping("/scheduled/{eventId}")
    public List<GameResponseDto> getGamesForEvent(@PathVariable int eventId) {
        return eventService.getGamesForEvent(eventId)
            .stream()
            .map(GameResponseDto::new)
            .collect(Collectors.toList());
    }
    @GetMapping("/scheduled/{gameId}")
    public List<EventResponseDto> getScheduledEventsForAGame(@PathVariable int gameId) {
        return eventService.getScheduledEventsForAGame(gameId)
            .stream()
            .map(EventResponseDto::new)
            .collect(Collectors.toList());
    }
    

    @PostMapping("/{eventId}/player/{playerId}")
    public void registerForEvent(@PathVariable int eventId, @PathVariable int playerId) {
        eventService.registerForEvent(eventId, playerId);
    }

    @DeleteMapping("/{eventId}/player/{playerId}")
    public void unregisterForEvent(@PathVariable int eventId, @PathVariable int playerId) {
        eventService.unregisterForEvent(eventId, playerId);
    }

    @GetMapping("/player/{playerId}")
    public List<EventResponseDto> getEventsForPlayer(@PathVariable int playerId) {
        return eventService.getEventsForPlayer(playerId)
                .stream()
                .map(EventResponseDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{eventId}/players")
    public List<PlayerResponseDto> getPlayersForEvent(@PathVariable int eventId) {
        return eventService.getPlayersForEvent(eventId)
                .stream()
                .map(PlayerResponseDto::new)
                .collect(Collectors.toList());
    }

}
