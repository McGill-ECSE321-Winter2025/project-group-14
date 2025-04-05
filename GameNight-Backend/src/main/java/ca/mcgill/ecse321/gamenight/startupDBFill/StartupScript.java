package ca.mcgill.ecse321.gamenight.startupDBFill;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import ca.mcgill.ecse321.gamenight.repo.*;
import ca.mcgill.ecse321.gamenight.model.*;

@Component
public class StartupScript implements CommandLineRunner {

    @Autowired
    private BorrowingRequestRepository borrowingRepo;

    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private GameCopyRepository gameCopyRepo;

    @Autowired
    private PersonRepository personRepo;

    @Autowired
    private GameOwnerRepository gameOwnerRepo;

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private GameReviewRepository reviewRepo;

    @Autowired
    private ScheduledGameRepository scheduleRepo;

    @Autowired
    private RegistrationRepository registrationRepo;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Application has started!");

        cleanup();

        Person person1 = personRepo.save(new Person("aaaaaa@gmail.com", "aaaaa",
        "Bertrand"));
        GameOwner owner1 = gameOwnerRepo.save(new GameOwner(person1));
        Player player1 = playerRepo.save(new Player(person1));

        Person person2 = personRepo.save(new Person("bbbbbb@gmail.com", "bbbbb",
        "Patrick"));
        GameOwner owner2 = gameOwnerRepo.save(new GameOwner(person2));
        Player player2 = playerRepo.save(new Player(person2));

        Game game1 = gameRepo.save(new Game("Batman", "A Batman game"));
        Game game2 = gameRepo.save(new Game("Uno", "A card game"));
        Game game3 = gameRepo.save(new Game("Monopoly", "A board game"));
        Game game4 = gameRepo.save(new Game("Jenga", "A fun block game"));
        Game game5 = gameRepo.save(new Game("Twister", "A game"));
        Game game6 = gameRepo.save(new Game("Sorry", "A multiplayer board game"));
        gameCopyRepo.save(new GameCopy("Perfect condition", game1, owner1));
        gameCopyRepo.save(new GameCopy("Missing piece", game2, owner1));
        gameCopyRepo.save(new GameCopy("Good condition", game3, owner1));
        gameCopyRepo.save(new GameCopy("Missing instructions", game4, owner1));

        reviewRepo.save(new GameReview(4, "Super fun game!!", player1, game1));
        reviewRepo.save(new GameReview(1, "Did not enjoy", player2, game1));
    }

    private void cleanup() {
        borrowingRepo.deleteAll();
        reviewRepo.deleteAll();
        scheduleRepo.deleteAll();
        registrationRepo.deleteAll();
        gameCopyRepo.deleteAll();
        gameRepo.deleteAll();
        gameOwnerRepo.deleteAll();
        playerRepo.deleteAll();
        personRepo.deleteAll();
    }
}
