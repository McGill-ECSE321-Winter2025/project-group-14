package ca.mcgill.ecse321.gamenight.controller;

import java.util.ArrayList;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gamenight.dto.GameCopyRequestDto;
import ca.mcgill.ecse321.gamenight.dto.GameCopyResponseDto;
import ca.mcgill.ecse321.gamenight.dto.GameRequestDto;
import ca.mcgill.ecse321.gamenight.dto.GameResponseDto;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.service.GameManagementService;

@RestController
public class GameManagementController {

    @Autowired
    GameManagementService gameManagementService;

    /**
     * Create a new game
     * 
     * @param game The game to create
     * @return The created game
     */
    @PostMapping("/games/")
    public GameResponseDto createGame(@RequestBody GameRequestDto game) {
        Game g = gameManagementService.createGame(game.getName(), game.getDescription());
        return new GameResponseDto(g);
    }

    /**
     * Return the game with the given ID
     * 
     * @param id The primary key of the game to find
     * @return The person with the given ID
     */
    @GetMapping("/games/{id}")
    public GameResponseDto findGameById(@PathVariable int id) {
        Game g = gameManagementService.findGameById(id);
        return new GameResponseDto(g);
    }

    /**
     * Update the game with the given ID
     * 
     * @param id The primary key of the game to update
     * @param game The updated game information
     * @return The updated game
     */
    @PutMapping("/games/{id}")
    public GameResponseDto updateGame(@PathVariable int id, @RequestBody GameRequestDto game) {
        // this will throw an exception if we try to create a game this way, should we change it?
        Game g = gameManagementService.updateGame(id, game.getName(), game.getDescription());
        return new GameResponseDto(g);
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
            games.add(new GameResponseDto(iterator.next()));
        }
        return games;
    }

    /**
     * Delete the game with the given ID from the system
     * 
     * @param id The primary key of the game to delete
     */
    @DeleteMapping("/games/{id}")
    public void deleteGame(@PathVariable int id) {
        gameManagementService.deleteGame(id);
    }

    /**
     * Create a new game copy
     * 
     * @param gameCopy The game copy to create
     * @return The created game copy
     */
    @PostMapping("/gamecopies/")
    public GameCopyResponseDto createGameCopy(@RequestBody GameCopyRequestDto gameCopy) {
        GameCopy g = gameManagementService.addGameCopy(gameCopy.getDescription(), gameCopy.getGameId(), gameCopy.getOwnerId());
        return new GameCopyResponseDto(g);
    }

    /**
     * Return the game copy with the given ID
     * 
     * @param id The primary key of the game copy
     * @return The game copy with the given ID
     */
    @GetMapping("/gamecopies/{id}")
    public GameCopyResponseDto findGameCopyById(@PathVariable int id) {
        GameCopy g = gameManagementService.findGameCopyById(id);
        return new GameCopyResponseDto(g);
    }

    /**
     * Update the game copy with the given ID
     * 
     * @param id The primary key of the game copy
     * @param gameCopy The updated game copy information
     * @return The updated game copy
     */
    @PutMapping("/gamecopies/{id}")
    public GameCopyResponseDto updateGameCopy(@PathVariable int id, @RequestBody GameCopyRequestDto gameCopy) {
        // this will throw an exception if we try to create a game this way, should we change it?
        GameCopy g = gameManagementService.updateGameCopy(id, gameCopy.getDescription());
        return new GameCopyResponseDto(g);
    }

    /**
     * Return all game copies of the game owner with the given ID
     * 
     * @param ownerId The primary key of the game owner
     * @return The game copies of the given game owner
     */
    @GetMapping("/gamecopies/")
    public ArrayList<GameCopyResponseDto> findGameCopyByOwner(@RequestParam(name = "owner_id") int ownerId) {
        ArrayList<GameCopyResponseDto> games = new ArrayList<>();
        Iterator<GameCopy> iterator = gameManagementService.findGameCopiesByOwner(ownerId).iterator();
        while (iterator.hasNext()) {
            games.add(new GameCopyResponseDto(iterator.next()));
        }
        return games;
    }
}
