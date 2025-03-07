package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import ca.mcgill.ecse321.gamenight.exception.GameNightException;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;

@SpringBootTest
public class GameManagementServiceTest {

    @Mock
    GameRepository gameRepository;

    @Mock
    GameCopyRepository gameCopyRepository;

    @Mock
    GameOwnerRepository gameOwnerRepository;

    @Mock
    PersonRepository personRepository;

    @InjectMocks
    GameManagementService gameManagementService;

    private GameOwner owner;


    @BeforeEach
    public  void setup() {
        Person p = new Person("hello@outlook.com", "pswd", "Alex");
        owner = new GameOwner(p);
        when(gameOwnerRepository.findById(owner.getId())).thenReturn(Optional.ofNullable(owner));
    }

    @Test
    public void createValidGameTest() {
        String name = "Uno";
        String description = "A card game";
        Game game = new Game(name, description);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game createdGame = gameManagementService.createGame(name, description);

        assertNotNull(createdGame);
        assertEquals(name, createdGame.getName());
        assertEquals(description, createdGame.getDescription());
    }

    @Test
    public void creatingGameWithNoNameTest() {
        GameNightException e = assertThrows(GameNightException.class, () ->
            gameManagementService.createGame(null, "A  card game"));
        
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("Game must have a name", e.getMessage());
    }

    @Test
    public void updateGameTest() {
        Game game = new Game("Uno", "A card game");
        when(gameRepository.findById(game.getId())).thenReturn(Optional.ofNullable(game));

        Game savedGame = gameManagementService.updateGame(game.getId(), "Monopoly", "A board game");

        assertNotNull(savedGame);
        assertEquals(game.getId(), savedGame.getId());
        assertEquals("Monopoly", savedGame.getName());
        assertEquals("A board game", savedGame.getDescription());
    }

    @Test
    public void findExistsingGameByIdTest() {
        Game game = new Game("Uno", "A card game");
        when(gameRepository.findById(game.getId())).thenReturn(Optional.ofNullable(game));

        Game foundGame = gameManagementService.findGameById(game.getId());

        assertEquals(game.getId(), foundGame.getId());
        assertEquals(game.getName(), foundGame.getName());
        assertEquals(game.getDescription(), foundGame.getDescription());
    }

    @Test
    public void tryToFindGameNotInDBTest() {
        int id = 5;
        when(gameRepository.findById(id)).thenReturn(Optional.ofNullable(null));

        GameNightException e = assertThrows(GameNightException.class, () ->
            gameManagementService.findGameById(id));
            
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("There is no game with ID " + id , e.getMessage());
    }

    @Test
    public void findAllGamesTest() {
        Game game1 = new Game("Uno", "A card game");
        Game game2 = new Game("Monopoly", "A board game");
        ArrayList<Game> gamesList = new ArrayList<>();
        gamesList.add(game1);
        gamesList.add(game2);
        when(gameRepository.findAll()).thenReturn(gamesList);

        Iterator<Game> games = gameManagementService.findAllGames().iterator();

        Game game1FromDB = games.next();
        assertNotNull(game1FromDB);
        assertEquals(game1.getId(), game1FromDB.getId());
        assertEquals(game1.getName(), game1FromDB.getName());
        assertEquals(game1.getDescription(), game1FromDB.getDescription());

        Game game2FromDB = games.next();
        assertNotNull(game2FromDB);
        assertEquals(game2.getId(), game2FromDB.getId());
        assertEquals(game2.getName(), game2FromDB.getName());
        assertEquals(game2.getDescription(), game2FromDB.getDescription());
    }

    @Test
    public void addGameCopyTest() {
        Game game = new Game("Uno", "A card game");
        when(gameRepository.findById(game.getId())).thenReturn(Optional.ofNullable(game));
        GameCopy gameCopy = new GameCopy("Lost a card", game, owner);
        when(gameCopyRepository.findById(gameCopy.getId())).thenReturn(Optional.ofNullable(gameCopy));

        GameCopy g = gameManagementService.addGameCopy("Lost a card", game.getId(), owner.getId());

        assertEquals(owner.getId(), g.getOwner().getId());
        assertEquals(game.getId(), g.getGame().getId());
        assertEquals("Lost a card", g.getDescription());
    }

    @Test
    public void updateGameCopyTest() {
        Game game = new Game("Uno", "A card game");
        GameCopy gameCopy = new GameCopy("Lost a card", game, owner);
        when(gameCopyRepository.findById(gameCopy.getId())).thenReturn(Optional.ofNullable(gameCopy));

        GameCopy savedGame = gameManagementService.updateGameCopy(gameCopy.getId(), "Lost two cards");

        assertEquals(gameCopy,savedGame);
    }

    @Test
    public void deleteGameCopyTest() {
        Game game = new Game("Uno", "A card game");
        GameCopy gameCopy = new GameCopy("Lost a card", game, owner);
        when(gameCopyRepository.findById(gameCopy.getId())).thenReturn(Optional.ofNullable(gameCopy));

        gameManagementService.deleteGameCopy(gameCopy.getId());

        verify(gameCopyRepository, times(1)).delete(gameCopy);
    }

    @Test
    public void findExistsingGameCopyByIdTest() {
        Game game = new Game("Uno", "A card game");
        GameCopy expected = new GameCopy("Lost a card", game, owner);
        when(gameCopyRepository.findById(expected.getId())).thenReturn(Optional.ofNullable(expected));

        GameCopy foundGame = gameManagementService.findGameCopyById(expected.getId());

        assertEquals(expected,foundGame);
    }

    @Test
    public void tryToFindGameCopyNotInDBTest() {
        int id = 5;
        when(gameCopyRepository.findById(id)).thenReturn(Optional.ofNullable(null));

        GameNightException e = assertThrows(GameNightException.class, () ->
            gameManagementService.findGameCopyById(id));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        assertEquals("There is no game copy with ID " + id , e.getMessage());
    }

    @Test
    public void findGameCopiesByOwnerTest() {
        when(gameOwnerRepository.findById(owner.getId())).thenReturn(Optional.ofNullable(owner));
        Game game1 = new Game("Uno", "A card game");
        GameCopy gameCopy1 = new GameCopy("aaa", game1, owner);
        Game game2 = new Game("Monopoly", "A board game");
        GameCopy gameCopy2 = new GameCopy("aaa", game2, owner);
        ArrayList<GameCopy> expected = new ArrayList<>();
        expected.add(gameCopy1);
        expected.add(gameCopy2);
        when(gameCopyRepository.findByGameOwner(owner)).thenReturn(expected);

        Iterable<GameCopy> result = gameManagementService.findGameCopiesByOwner(owner.getId());

        assertEquals(expected, result);
    }
}
