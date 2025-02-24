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

    @PostMapping("/games/")
    public GameResponseDto createGame(@RequestBody GameRequestDto game) {
        Game g = gameManagementService.createGame(game.getName(), game.getDescription());
        return new GameResponseDto(g);
    }

    @GetMapping("/games/{id}")
    public GameResponseDto findGameById(@PathVariable int id) {
        Game g = gameManagementService.findGameById(id);
        return new GameResponseDto(g);
    }

    @PutMapping("/games/{id}")
    public GameResponseDto updateGame(@PathVariable int id, @RequestBody GameRequestDto game) {
        // this will throw an exception if we try to create a game this way, should we change it?
        Game g = gameManagementService.updateGame(id, game.getName(), game.getDescription());
        return new GameResponseDto(g);
    }

    @GetMapping("/games/{id}")
    public ArrayList<GameResponseDto> findAllGames() {
        ArrayList<GameResponseDto> games = new ArrayList<GameResponseDto>();
        Iterator<Game> iterator = gameManagementService.findAllGames().iterator();
        while (iterator.hasNext()) {
            games.add(new GameResponseDto(iterator.next()));
        }
        return games;
    }

    @DeleteMapping("/games/{id}")
    public void deleteGame(@PathVariable int id) {
        gameManagementService.deleteGame(id);
    }

    @PostMapping("/gamecopies/")
    public GameCopyResponseDto createGameCopy(@RequestBody GameCopyRequestDto game) {
        GameCopy g = gameManagementService.addGameCopy(game.getDescription(), game.getGameId(), game.getOwnerId());
        return new GameCopyResponseDto(g);
    }

    @GetMapping("/gamecopies/{id}")
    public GameCopyResponseDto findGameCopyById(@PathVariable int id) {
        GameCopy g = gameManagementService.findGameCopyById(id);
        return new GameCopyResponseDto(g);
    }

    @PutMapping("/gamecopies/{id}")
    public GameCopyResponseDto updateGameCopy(@PathVariable int id, @RequestBody GameCopyRequestDto game) {
        // this will throw an exception if we try to create a game this way, should we change it?
        GameCopy g = gameManagementService.updateGameCopy(id, game.getDescription());
        return new GameCopyResponseDto(g);
    }

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
