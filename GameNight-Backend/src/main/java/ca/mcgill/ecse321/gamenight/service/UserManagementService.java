package ca.mcgill.ecse321.gamenight.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.model.AccountRole;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import jakarta.transaction.Transactional;

@Service
public class UserManagementService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private GameOwnerRepository gameOwnerRepository;

    public UserManagementService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person registerUser(String email, String password, String name) throws FirebaseAuthException {
        // Check if user already exists
        if (personRepository.findPersonByEmailAddress(email) != null) {
            throw new IllegalArgumentException("User already exists with this email.");
        }

        // Create user in Firebase
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setDisplayName(name);

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

        // Save user in the database
        Person person = new Person(email, password, name, userRecord.getUid());
        personRepository.save(person);

        return person;
    }

    public Person getUserByFirebaseUid(String firebaseUid) {
        return personRepository.findPersonByFirebaseUid(firebaseUid);
    }

    public String loginUser(String email) throws FirebaseAuthException {
        // Firebase handles login via client-side SDK
        UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
        return userRecord.getUid();
    }

    public void deleteUser(String firebaseUid) throws FirebaseAuthException {
        // Delete from Firebase
        FirebaseAuth.getInstance().deleteUser(firebaseUid);

        // Delete from database
        Person person = personRepository.findPersonByFirebaseUid(firebaseUid);
        if (person != null) {
            personRepository.delete(person);
        }
    }

    public String verifyFirebaseToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken.replace("Bearer ", ""));
        return decodedToken.getUid();
    }

    @Transactional
    public void createPlayer() {
        Player newPlayer = new Player();
        playerRepository.save(newPlayer);
    }

    @Transactional
    public void updatePlayerDetails(Person person, String email, String passWord) {
        if (!email.equals(person.getEmailAddress())) {
            person.setEmailAddress(email);
        }
        if (!passWord.equals(person.getPassword())) {
            person.setPassword(passWord);
        }
    }

    @Transactional
    public void deletePlayerDetails(Person person) {
        int playerId = person.getId();
        if (!playerRepository.existsById(playerId)) {
            throw new IllegalArgumentException("Player with ID " + playerId + " does not exist.");
        }
        playerRepository.deleteById(playerId);
    }

    @Transactional
    public void createGameOwner() {
        GameOwner newGameOwner = new GameOwner();
        gameOwnerRepository.save(newGameOwner);
    }

    @Transactional
    public void updateGameOwnerDetails(Person person, String email, String passWord) {
        if (!email.equals(person.getEmailAddress())) {
            person.setEmailAddress(email);
        }
        if (!passWord.equals(person.getPassword())) {
            person.setPassword(passWord);
        }
    }

    @Transactional
    public void deleteGameOwnerDetails(Person person) {
        int gameOwnerId = person.getId();
        if (!gameOwnerRepository.existsById(gameOwnerId)) {
            throw new IllegalArgumentException("Game Owner with ID " + gameOwnerId + " does not exist.");
        }
        gameOwnerRepository.deleteById(gameOwnerId);
    }

    @Transactional
    public void toggleAccountRole(Person person, String role) {
        if (role.equalsIgnoreCase("gameowner")) {
            toggleToGameOwner(person.getId());
        } else if (role.equalsIgnoreCase("player")) {
            toggleToPlayer(person.getId());
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    private void toggleToGameOwner(int personId) {
        Optional<GameOwner> gameOwnerOpt = gameOwnerRepository.findById(personId);

        if (gameOwnerOpt.isPresent()) {
            GameOwner gameOwner = gameOwnerOpt.get();
            gameOwner.setActive(true);
            gameOwnerRepository.save(gameOwner);
        } else {
            throw new IllegalArgumentException("Person is not a GameOwner.");
        }
        Optional<Player> playerOpt = playerRepository.findById(personId);
        playerOpt.ifPresent(player -> {
            player.setActive(false);
            playerRepository.save(player);
        });
    }

    private void toggleToPlayer(int personId) {
        Optional<Player> playerOpt = playerRepository.findById(personId);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            player.setActive(true);
            playerRepository.save(player);
        } else {
            throw new IllegalArgumentException("Person is not a Player.");
        }
        Optional<GameOwner> gameOwnerOpt = gameOwnerRepository.findById(personId);
        gameOwnerOpt.ifPresent(gameOwner -> {
            gameOwner.setActive(false);
            gameOwnerRepository.save(gameOwner);
        });
    }

    public List<Person> getAllUsers() {
        Iterable<Person> iterable = personRepository.findAll();
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }

    public Person getUserById(int userId) {
        return personRepository.findPersonById(userId);
    }

}
