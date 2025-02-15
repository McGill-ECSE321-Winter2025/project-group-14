package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    public void clearDatabase() {
        gameCopyRepo.deleteAll();
        gameRepo.deleteAll();
        gameOwnerRepo.deleteAll();
        personRepo.deleteAll();
    }

    @Test
    public void testCreateAndReadGameCopy() {
        // Create a Person
        Person person = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person);

        // Create Owner
        GameOwner owner = new GameOwner();
        owner.setPerson(person);
        gameOwnerRepo.save(owner);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Read the GameCopy from the database
        GameCopy retrievedGameCopy = gameCopyRepo.findById(gameCopy.getId()).orElse(null);

        // Assertions
        assertNotNull(retrievedGameCopy);
        assertEquals(gameCopy.getDescription(), retrievedGameCopy.getDescription());
        assertEquals(gameCopy.getGame().getId(), retrievedGameCopy.getGame().getId());
        assertEquals(gameCopy.getOwner().getPerson().getName(), retrievedGameCopy.getOwner().getPerson().getName());
    }

    @Test
    public void testModifyGameCopy() {
        // Create a Person
        Person person = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person);

        // Create Owner
        GameOwner owner = new GameOwner();
        owner.setPerson(person);
        gameOwnerRepo.save(owner);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Modify the GameCopy's description
        String updatedDescription = "Updated description of Batman";
        gameCopy.setDescription(updatedDescription);
        gameCopyRepo.save(gameCopy);

        // Retrieve the updated GameCopy
        GameCopy updatedGameCopy = gameCopyRepo.findById(gameCopy.getId()).orElse(null);

        // Assertions
        assertNotNull(updatedGameCopy);
        assertEquals(updatedDescription, updatedGameCopy.getDescription());
        assertEquals(gameCopy.getGame().getId(), updatedGameCopy.getGame().getId());
        assertEquals(gameCopy.getOwner().getPerson().getName(), updatedGameCopy.getOwner().getPerson().getName());
    }

    @Test
    public void testDeleteGameCopy() {
        // Create a Person
        Person person = new Person("aaaaaa@gmail.com", "aaaaa", "Bertrand");
        personRepo.save(person);

        // Create Owner
        GameOwner owner = new GameOwner();
        owner.setPerson(person);
        gameOwnerRepo.save(owner);

        // Create a Game
        Game game = new Game("Batman", "A Batman game");
        gameRepo.save(game);

        // Create a GameCopy
        GameCopy gameCopy = new GameCopy("My copy of Batman", game, owner);
        gameCopyRepo.save(gameCopy);

        // Delete the GameCopy
        gameCopyRepo.delete(gameCopy);

        // Try to retrieve the deleted GameCopy
        GameCopy deletedGameCopy = gameCopyRepo.findById(gameCopy.getId()).orElse(null);

        // Assertions
        assertNull(deletedGameCopy);
    }
}