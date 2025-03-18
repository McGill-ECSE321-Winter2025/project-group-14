package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;

@SpringBootTest
public class GameCopyRepositoryTest {

    @Autowired
    private GameCopyRepository gameCopyRepo;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private GameOwnerRepository gameOwnerRepo;

    @Autowired
    private PersonRepository personRepo;

    private Person person;
    private GameOwner owner;
    private Game game;

    @BeforeEach
    public void setup() {
        clearDatabase();

        // Create and save a Person
        person = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person);

        // Create and save a GameOwner
        owner = new GameOwner(person);
        gameOwnerRepo.save(owner);

        // Create and save a Game
        game = new Game("Batman", "A Batman game");
        gameRepo.save(game);
    }

    @AfterEach
    public void clearDatabase() {
        gameCopyRepo.deleteAll();
        gameRepo.deleteAll();
        gameOwnerRepo.deleteAll();
        personRepo.deleteAll();
    }

    @Test
    public void testCreateAndReadGameCopy() {
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        GameCopy retrievedGameCopy = gameCopyRepo.findById(gameCopy.getId()).orElse(null);

        assertNotNull(retrievedGameCopy);
        assertEquals(gameCopy.getDescription(), retrievedGameCopy.getDescription());
        assertEquals(gameCopy.getGame().getId(), retrievedGameCopy.getGame().getId());
        assertEquals(gameCopy.getOwner().getPerson().getName(), retrievedGameCopy.getOwner().getPerson().getName());
    }

    @Test
    public void testModifyGameCopy() {
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        String updatedDescription = "Updated description of Batman";
        gameCopy.setDescription(updatedDescription);
        gameCopyRepo.save(gameCopy);

        GameCopy updatedGameCopy = gameCopyRepo.findById(gameCopy.getId()).orElse(null);

        assertNotNull(updatedGameCopy);
        assertEquals(updatedDescription, updatedGameCopy.getDescription());
        assertEquals(gameCopy.getGame().getId(), updatedGameCopy.getGame().getId());
        assertEquals(gameCopy.getOwner().getPerson().getName(), updatedGameCopy.getOwner().getPerson().getName());
    }

    @Test
    public void testDeleteGameCopy() {
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        gameCopyRepo.delete(gameCopy);

        GameCopy deletedGameCopy = gameCopyRepo.findById(gameCopy.getId()).orElse(null);
        assertNull(deletedGameCopy);
    }

    @Test
    public void testFindByGame() {
        GameCopy gameCopy1 = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy1);

        GameCopy gameCopy2 = new GameCopy("Another copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy2);

        List<GameCopy> gameCopies = gameCopyRepo.findByGame(game);

        assertNotNull(gameCopies);
        assertEquals(2, gameCopies.size());
        assertEquals(gameCopy1.getDescription(), gameCopies.get(0).getDescription());
        assertEquals(gameCopy2.getDescription(), gameCopies.get(1).getDescription());
    }

    @Test
    public void testFindByGameOwner() {
        GameCopy gameCopy1 = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy1);

        GameCopy gameCopy2 = new GameCopy("Another copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy2);

        List<GameCopy> gameCopies = gameCopyRepo.findByGameOwner(owner);

        assertNotNull(gameCopies);
        assertEquals(2, gameCopies.size());
        assertEquals(gameCopy1.getDescription(), gameCopies.get(0).getDescription());
        assertEquals(gameCopy2.getDescription(), gameCopies.get(1).getDescription());
    }
}