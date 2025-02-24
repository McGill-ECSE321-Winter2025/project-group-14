package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;

@SpringBootTest
public class GameManagementServiceTest {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    GameCopyRepository gameCopyRepository;

    @Autowired
    GameManagementService gameManagementService;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    GameOwnerRepository gameOwnerRepository;

    private GameOwner owner;

    @AfterEach
    public void clearDatabase() {
        gameCopyRepository.deleteAll();
        gameRepository.deleteAll();
        gameOwnerRepository.deleteAll();
        personRepository.deleteAll();
    }

    @BeforeEach
    public  void setup() {
        Person p = new Person("hello@outlook.com", "pswd", "Alex");
        owner = new GameOwner(p);
    }

    @Test
    public void createValidGameTest() {
        String name = "Uno";
        String description = "A card game";

        Game createdGame = gameManagementService.createGame(name, description);

        // check that the instance returned has the correct information
        assertNotNull(createdGame);
        assertEquals(name, createdGame.getName());
        assertEquals(description, createdGame.getDescription());

        // check that the game was successfully added to the database
        Optional<Game> game = gameRepository.findById(createdGame.getId());
        assertNotNull(game);
        Game gameInDB = game.get();
        assertEquals(createdGame.getId(), gameInDB.getId());
        assertEquals(createdGame.getName(), gameInDB.getName());
        assertEquals(createdGame.getDescription(), gameInDB.getDescription());
    }
}
