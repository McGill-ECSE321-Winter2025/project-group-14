package ca.mcgill.ecse321.gamenight.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import jakarta.transaction.Transactional;

@Service
public class GameManagementService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Transactional
    public Game createGame(String name, String description) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Game must have a name");
        }
        Game game = new Game(name, description);
        gameRepository.save(game);
        return game;
    }

    @Transactional
    public Game updateGame(int id, String name, String description) {
        Game game = findGameById(id);
        game.setName(name);
        game.setDescription(description);
        gameRepository.save(game);
        return game;
    }

    public Game findGameById(int id) {
        Optional<Game> g = gameRepository.findById(id);
        if (!g.isPresent()) {
            throw new IllegalArgumentException("There is no game with ID " + id);
        }
        return g.get();
    }

    public Iterable<Game> findAllGames() {
        Iterable<Game> games = gameRepository.findAll();
        return games;
    }

    @Transactional
    public GameCopy addGameCopy(String description, Game game, GameOwner owner) {
        GameCopy g = new GameCopy(description, game, owner);
        return g;
    }

    @Transactional
    public GameCopy updateGameCopyDescription(int id, String description) {
        GameCopy g = findGameCopyById(id);
        g.setDescription(description);
        gameCopyRepository.save(g);
        return g;
    }

    @Transactional
    public void deleteGameCopy(int id) {
        Optional<GameCopy> g = gameCopyRepository.findById(id);
        if (g.isPresent()) {
            gameCopyRepository.delete(g.get());
        }
    }

    public GameCopy findGameCopyById(int id) {
        Optional<GameCopy> g = gameCopyRepository.findById(id);
        if (!g.isPresent()) {
            throw new IllegalArgumentException("There is no game copy with ID " + id);
        }
        return g.get();
    }

    public List<GameCopy> findGameCopiesForOwner(GameOwner gameOwner) {
        List<GameCopy> games = gameCopyRepository.findByGameOwner(gameOwner);
        return games;
    }
}
