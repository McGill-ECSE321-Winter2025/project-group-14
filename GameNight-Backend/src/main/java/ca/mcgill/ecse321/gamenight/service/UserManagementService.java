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
        Person person = new Person(email, password, name);
        person.setFirebaseUid(userRecord.getUid());
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
            personRepository.delete(person); // this will also delete the related
        }
    }

    public String verifyFirebaseToken(String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken.replace("Bearer ", ""));
        return decodedToken.getUid();
    }

    @Transactional
    public Player createPlayer(Person person) {
        Player newPlayer = new Player(person);
        playerRepository.save(newPlayer);
        return newPlayer;
    }

    @Transactional
    public void updatePlayer(Person person, String email, String passWord) {
        person.setEmailAddress(email);
        person.setPassword(passWord);
        personRepository.save(person);
        
    }

    @Transactional
    public void deletePlayer(int playerId) {
        if (playerRepository.existsById(playerId)) {
            playerRepository.deleteById(playerId);
        }
    }

    @Transactional
    public GameOwner createGameOwner(Person person) {
        GameOwner newGameOwner = new GameOwner(person);
        gameOwnerRepository.save(newGameOwner);
        return newGameOwner;
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
    public void deleteGameOwner(int gameOwnerId) {

        if (!gameOwnerRepository.existsById(gameOwnerId)) {
            throw new IllegalArgumentException("Game Owner with ID " + gameOwnerId + " does not exist.");
        }
        gameOwnerRepository.deleteById(gameOwnerId);
    }
    
    public void toggleAccountRole(int id) {
        Person person = personRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Person not found with ID: " + id));
    
        person.setGameOwner(!person.isGameOwner()); // Toggle role
        personRepository.save(person);
    }
    
    public List<Person> getAllUsers() {
        Iterable<Person> iterable = personRepository.findAll();
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }

    public Person getUserById(int userId) {
        return personRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Person not found with ID: " + userId));
    }
    
}
