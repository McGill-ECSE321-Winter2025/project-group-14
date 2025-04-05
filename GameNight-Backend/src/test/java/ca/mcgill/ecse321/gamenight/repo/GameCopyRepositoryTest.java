package ca.mcgill.ecse321.gamenight.repo;

import static org.junit.jupiter.api.Assertions.*; 

import java.util.List;
import java.util.Optional; 


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional; 

import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player; 
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;



@SpringBootTest
@Transactional 
public class GameCopyRepositoryTest {

    @Autowired private GameCopyRepository gameCopyRepo;
    @Autowired private GameRepository gameRepo;
    @Autowired private GameOwnerRepository gameOwnerRepo;
    @Autowired private PersonRepository personRepo;
    @Autowired private PlayerRepository playerRepo; 


    private Person person;
    private GameOwner owner;
    private Game game;

    @BeforeEach
    public void setup() {
        person = new Person("gcopy_owner@gmail.com", "pw_gcopy", "Gcopy Owner");
        person = personRepo.save(person);

        owner = new GameOwner();
        owner.setPerson(person); 
        owner = gameOwnerRepo.save(owner);

        game = new Game("Gcopy Test Game", "A game for Gcopy tests");
        game = gameRepo.save(game);
    }


    @Test
    public void testCreateAndReadGameCopy() {
        GameCopy gameCopy = new GameCopy("My copy of Gcopy Test Game", game, owner);
        gameCopy = gameCopyRepo.save(gameCopy); 

        Optional<GameCopy> retrievedGameCopyOpt = gameCopyRepo.findById(gameCopy.getId());
        assertTrue(retrievedGameCopyOpt.isPresent());
        GameCopy retrievedGameCopy = retrievedGameCopyOpt.get();

        assertNotNull(retrievedGameCopy);
        assertEquals(gameCopy.getDescription(), retrievedGameCopy.getDescription());
        assertNotNull(retrievedGameCopy.getGame());
        assertEquals(game.getId(), retrievedGameCopy.getGame().getId());
        assertNotNull(retrievedGameCopy.getOwner());
        assertNotNull(retrievedGameCopy.getOwner().getPerson());
        assertEquals(owner.getPerson().getName(), retrievedGameCopy.getOwner().getPerson().getName());
    }

    @Test
    public void testModifyGameCopy() {
        GameCopy gameCopy = new GameCopy("My modifiable copy", game, owner);
        gameCopy = gameCopyRepo.save(gameCopy);
        int copyId = gameCopy.getId(); 

        String updatedDescription = "Updated description";
        
        Optional<GameCopy> toUpdateOpt = gameCopyRepo.findById(copyId); 
        assertTrue(toUpdateOpt.isPresent());
        GameCopy toUpdate = toUpdateOpt.get();
        
        toUpdate.setDescription(updatedDescription);
        gameCopyRepo.save(toUpdate);

        Optional<GameCopy> updatedGameCopyOpt = gameCopyRepo.findById(copyId);
        assertTrue(updatedGameCopyOpt.isPresent());
        GameCopy updatedGameCopy = updatedGameCopyOpt.get();

        assertNotNull(updatedGameCopy);
        assertEquals(updatedDescription, updatedGameCopy.getDescription());
        assertEquals(game.getId(), updatedGameCopy.getGame().getId());
        assertEquals(owner.getPerson().getName(), updatedGameCopy.getOwner().getPerson().getName());
    }

    @Test
    public void testDeleteGameCopy() {
        GameCopy gameCopy = new GameCopy("To be deleted copy", game, owner);
        gameCopy = gameCopyRepo.save(gameCopy);
        int copyId = gameCopy.getId();

        Optional<GameCopy> toDeleteOpt = gameCopyRepo.findById(copyId);
        assertTrue(toDeleteOpt.isPresent());
        gameCopyRepo.delete(toDeleteOpt.get()); 

        Optional<GameCopy> deletedGameCopyOpt = gameCopyRepo.findById(copyId);
        assertFalse(deletedGameCopyOpt.isPresent()); 
    }

    @Test
    public void testFindByGame() {
        GameCopy gameCopy1 = new GameCopy("GC FindByGame 1", game, owner);
        gameCopyRepo.save(gameCopy1);
        int gameCopy1Id = gameCopy1.getId(); 

        GameCopy gameCopy2 = new GameCopy("GC FindByGame 2", game, owner);
        gameCopyRepo.save(gameCopy2);
        int gameCopy2Id = gameCopy2.getId(); 
        
        Game otherGame = new Game("Other Game", "...");
        gameRepo.save(otherGame);
        GameCopy otherCopy = new GameCopy("Other Copy", otherGame, owner);
        gameCopyRepo.save(otherCopy);

        List<GameCopy> gameCopies = gameCopyRepo.findByGame(game);

        assertNotNull(gameCopies);
        assertEquals(2, gameCopies.size());
        assertTrue(gameCopies.stream().anyMatch(gc -> gc.getId() == gameCopy1Id));
        assertTrue(gameCopies.stream().anyMatch(gc -> gc.getId() == gameCopy2Id));
    }

    @Test
    public void testFindByGameOwner() {
        GameCopy gameCopy1 = new GameCopy("GC FindByOwner 1", game, owner);
        gameCopyRepo.save(gameCopy1);
        int gameCopy1Id = gameCopy1.getId(); 

        GameCopy gameCopy2 = new GameCopy("GC FindByOwner 2", game, owner);
        gameCopyRepo.save(gameCopy2);
        int gameCopy2Id = gameCopy2.getId(); 
        
        Person otherPerson = new Person("otherowner@gcopy.com", "pw", "Other Owner");
        personRepo.save(otherPerson);
        GameOwner otherOwner = new GameOwner(); otherOwner.setPerson(otherPerson);
        gameOwnerRepo.save(otherOwner);
        GameCopy otherCopy = new GameCopy("Other Owner Copy", game, otherOwner);
        gameCopyRepo.save(otherCopy);

        List<GameCopy> gameCopies = gameCopyRepo.findByGameOwner(owner);

        assertNotNull(gameCopies);
        assertEquals(2, gameCopies.size());
        assertTrue(gameCopies.stream().anyMatch(gc -> gc.getId() == gameCopy1Id));
        assertTrue(gameCopies.stream().anyMatch(gc -> gc.getId() == gameCopy2Id));
    }
}