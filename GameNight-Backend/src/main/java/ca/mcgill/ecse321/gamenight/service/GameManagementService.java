package ca.mcgill.ecse321.gamenight.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ca.mcgill.ecse321.gamenight.exception.MissingFieldsException;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.BorrowingRequestRepository;
import jakarta.transaction.Transactional;

@Service
public class GameManagementService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private GameOwnerRepository gameOwnerRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private BorrowingRequestRepository borrowingRequestRepository;

    @Transactional
    public Game createGame(String name, String description, MultipartFile imageFile) throws IOException {
        if (name == null || name.isEmpty()) {
            throw new MissingFieldsException("Game must have a name");
        }

        Game game = new Game(name, description);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = fileStorageService.store(imageFile, "game");
            game.setImagePath(imagePath);
        }

        gameRepository.save(game);
        return game;
    }

    @Transactional
    public Game updateGame(int id, String name, String description, MultipartFile imageFile) throws IOException {
        Game game = findGameById(id);
        game.setName(name);
        game.setDescription(description);

        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (game.getImagePath() != null) {
                fileStorageService.delete(game.getImagePath());
            }
            // Store new image
            String imagePath = fileStorageService.store(imageFile, "game");
            game.setImagePath(imagePath);
        }

        gameRepository.save(game);
        return game;
    }

    public Game findGameById(int id) throws ObjectNotFoundException {
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
        Optional<GameCopy> copy = gameCopyRepository.findById(id);
        if (copy.isPresent()) {
            List<BorrowingRequest> requests = borrowingRequestRepository.findByGameCopy(copy.get());
            for (BorrowingRequest request : requests) {
                borrowingRequestRepository.delete(request);
            }
        }
        gameCopyRepository.deleteById(id);

    }

    public GameCopy findGameCopyById(int id) throws ObjectNotFoundException {
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

    public List<GameCopy> findGameCopiesByGame(int gameID) {
        Optional<Game> game = gameRepository.findById(gameID);
        if (game.isEmpty()) {
            throw new ObjectNotFoundException("There is no game with ID " + gameID);
        }
        return gameCopyRepository.findByGame(game.get());
    }

    private GameOwner getGameOwnerById(int ownerId) throws ObjectNotFoundException {
        Optional<GameOwner> owner = gameOwnerRepository.findById(ownerId);
        if (owner.isEmpty()) {
            throw new ObjectNotFoundException("There is no owner with ID " + ownerId);
        }
        return owner.get();
    }

    public int getGameOwnerIdByPersonId(int personId) throws ObjectNotFoundException {
        GameOwner owner = gameOwnerRepository.findByPersonId(personId);
        if (owner == null) {
            throw new ObjectNotFoundException("There is no owner with ID " + personId);
        }
        return owner.getId();
    }

    public Resource getGameImage(int gameId) throws IOException {
        Game game = findGameById(gameId);
        if (game.getImagePath() == null) {
            throw new ObjectNotFoundException("No image found for game with ID " + gameId);
        }
        return fileStorageService.load(game.getImagePath());
    }
}
