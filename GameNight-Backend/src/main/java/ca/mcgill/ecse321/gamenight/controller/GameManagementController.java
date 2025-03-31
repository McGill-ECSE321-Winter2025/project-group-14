package ca.mcgill.ecse321.gamenight.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gamenight.dto.GameCopyRequestDto;
import ca.mcgill.ecse321.gamenight.dto.GameCopyResponseDto;
import ca.mcgill.ecse321.gamenight.dto.GameRequestDto;
import ca.mcgill.ecse321.gamenight.dto.GameResponseDto;
import ca.mcgill.ecse321.gamenight.middleware.RequireUser;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.service.GameManagementService;
import ca.mcgill.ecse321.gamenight.service.GameReviewService;

/**
 * REST controller for managing games and game copies
 * 
 * This controller handles endpoints related to creating, accessing, updating,
 * and deleting games and game copies.
 */
@RestController
public class GameManagementController {

    @Autowired
    GameManagementService gameManagementService;

    @Autowired
    GameReviewService reviewService;

    /**
     * Create a new game
     * 
     * @param game The game to create
     * @return The created game
     */
    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    @RequireUser
    public GameResponseDto createGame(@RequestBody GameRequestDto game) {
        Game g = gameManagementService.createGame(game.getName(), game.getDescription());
        return new GameResponseDto(g, 0);
    }

    /**
     * Return the game with the given ID
     * 
     * @param id The primary key of the game to find
     * @return The game with the given ID
     */
    @GetMapping("/games/{id}")
    @RequireUser
    public GameResponseDto findGameById(@PathVariable int id) {
        Game g = gameManagementService.findGameById(id);
        int rating = (int) reviewService.getAverageRatingForGame(g) / 5;
        return new GameResponseDto(g, rating);
    }

    /**
     * Update the game with the given ID
     * 
     * @param id   The primary key of the game to update
     * @param game The updated game information
     * @return The updated game
     */
    @PutMapping("/games/{id}")
    @RequireUser
    public GameResponseDto updateGame(@PathVariable int id, @RequestBody GameRequestDto game) {
        Game g = gameManagementService.updateGame(id, game.getName(), game.getDescription());
        int rating = (int) reviewService.getAverageRatingForGame(g) / 5;
        return new GameResponseDto(g, rating);
    }

    /**
     * Return all games in the system
     * 
     * @return All games in the system
     */
    @GetMapping("/games")
    public ArrayList<GameResponseDto> findAllGames() {
        ArrayList<GameResponseDto> games = new ArrayList<GameResponseDto>();
        Iterator<Game> iterator = gameManagementService.findAllGames().iterator();
        while (iterator.hasNext()) {
            Game game = iterator.next();
            int rating = (int) reviewService.getAverageRatingForGame(game) / 5;
            games.add(new GameResponseDto(game, rating));
        }
        return games;
    }

    /**
     * Create a new game copy
     * 
     * @param gameCopy The game copy to create
     * @return The created game copy
     */
    @PostMapping("/game-copies/")
    @ResponseStatus(HttpStatus.CREATED)
    @RequireUser
    public GameCopyResponseDto createGameCopy(@RequestBody GameCopyRequestDto gameCopy) {
        int ownerId = gameManagementService.getGameOwnerIdByPersonId(gameCopy.getOwnerId());
        GameCopy g = gameManagementService.createGameCopy(gameCopy.getDescription(), gameCopy.getGameId(),
                ownerId);
        return new GameCopyResponseDto(g);
    }

    /**
     * Return the game copy with the given ID
     * 
     * @param id The primary key of the game copy
     * @return The game copy with the given ID
     */
    @GetMapping("/game-copies/{id}")
    @RequireUser
    public GameCopyResponseDto findGameCopyById(@PathVariable int id) {
        GameCopy g = gameManagementService.findGameCopyById(id);
        return new GameCopyResponseDto(g);
    }

    /**
     * Update the game copy with the given ID
     * 
     * @param id       The primary key of the game copy
     * @param gameCopy The updated game copy information
     * @return The updated game copy
     */
    @PutMapping("/game-copies/{id}")
    @RequireUser
    public GameCopyResponseDto updateGameCopy(@PathVariable int id, @RequestBody GameCopyRequestDto gameCopy) {
        GameCopy g = gameManagementService.updateGameCopy(id, gameCopy.getDescription());
        return new GameCopyResponseDto(g);
    }

    /**
     * Return all game copies of the game owner with the given ID
     * 
     * @param ownerId The primary key of the game owner
     * @return The game copies of the given game owner
     */
    @GetMapping("/game-copies")
    @RequireUser
    public ArrayList<GameCopyResponseDto> findGameCopyByOwner(@RequestParam(name = "owner_id") int personId) {
        int ownerId = gameManagementService.getGameOwnerIdByPersonId(personId);
        ArrayList<GameCopyResponseDto> games = new ArrayList<>();
        Iterator<GameCopy> iterator = gameManagementService.findGameCopiesByOwner(ownerId).iterator();
        while (iterator.hasNext()) {
            games.add(new GameCopyResponseDto(iterator.next()));
        }
        return games;
    }

    /**
     * Delete the game copy with the given ID from the system
     * 
     * @param id The primary key of the game copy to delete
     */
    @DeleteMapping("/game-copies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequireUser
    public void deleteGameCopy(@PathVariable int id) {
        gameManagementService.deleteGameCopy(id);
    }

    /**
     * Get all the copies of a given game
     * 
     * @param gameId The id of the game
     * @return The game copies of the given game
     */
    @GetMapping("game/{gameId}/game-copies")
    @RequireUser
    public List<GameCopyResponseDto> findGameCopiesByGame(@PathVariable int gameId) {
        ArrayList<GameCopyResponseDto> response = new ArrayList<>();
        List<GameCopy> gameCopies = gameManagementService.findGameCopiesByGame(gameId);
        for (GameCopy gameCopy : gameCopies) {
            response.add(new GameCopyResponseDto(gameCopy));
        }
        return response;
    }
}
