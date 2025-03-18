package ca.mcgill.ecse321.gamenight.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.exception.MissingFieldsException;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import jakarta.transaction.Transactional;

@Service
public class GameManagementService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private GameOwnerRepository gameOwnerRepository;

    @Transactional
    public Game createGame(String name, String description) {
        if (name == null || name.isEmpty()) {
            throw new MissingFieldsException("Game must have a name");
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
        if (g.isEmpty()) {
            throw new ObjectNotFoundException("There is no game with ID " + id);
        }
        return g.get();
    }

    public Iterable<Game> findAllGames() {
        return gameRepository.findAll();
    }

    @Transactional
    public GameCopy createGameCopy(String description, int gameId, int ownerId) {
        GameOwner gameOwner = getGameOwnerById(ownerId);
        Game game = findGameById(gameId);
        GameCopy newGameCopy = new GameCopy(description, game, gameOwner);
        gameCopyRepository.save(newGameCopy);
        return newGameCopy;
    }

    @Transactional
    public GameCopy updateGameCopy(int id, String description) {
        // can only modify the description, otherwise it should be deleted
        GameCopy gameCopy = findGameCopyById(id);
        gameCopy.setDescription(description);
        gameCopyRepository.save(gameCopy);
        return gameCopy;
    }

    @Transactional
    public void deleteGameCopy(int id) {
        gameCopyRepository.deleteById(id);
    }

    public GameCopy findGameCopyById(int id) {
        Optional<GameCopy> g = gameCopyRepository.findById(id);
        if (!g.isPresent()) {
            throw new ObjectNotFoundException("There is no game copy with ID " + id);
        }
        return g.get();
    }

    public List<GameCopy> findGameCopiesByOwner(int ownerId) {
        GameOwner gameOwner = getGameOwnerById(ownerId);
        return gameCopyRepository.findByGameOwner(gameOwner);
    }

    private GameOwner getGameOwnerById(int ownerId) {
        // TODO: replace this once the service is there???
        Optional<GameOwner> owner = gameOwnerRepository.findById(ownerId);
        if (owner.isEmpty()) {
            throw new ObjectNotFoundException("There is no owner with ID " + ownerId);
        }
        return owner.get();
    }
}
