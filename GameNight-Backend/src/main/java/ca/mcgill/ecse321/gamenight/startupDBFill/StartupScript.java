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

        Game game1 = new Game("Batman", "A Batman game");
        game1.setImagePath("games/batman.jpg");
        gameRepo.save(game1);
        Game game2 = new Game("Uno", "A card game");
        game2.setImagePath("games/uno.jpg");
        gameRepo.save(game2);
        Game game3 = new Game("Monopoly", "A board game");
        game3.setImagePath("games/monopoly.jpg");
        gameRepo.save(game3);
        Game game4 = new Game("Jenga", "A fun block game");
        game4.setImagePath("games/jenga.jpg");
        gameRepo.save(game4);
        Game game5 = new Game("Twister", "A game");
        game5.setImagePath("games/twister.jpg");
        gameRepo.save(game5);
        Game game6 = new Game("Sorry", "A multiplayer board game");
        game6.setImagePath("games/sorry.webp");
        gameRepo.save(game6);

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
