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

    @Transactional
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

    @Transactional
    public boolean updateUser(int id, String oldPassword, String newEmail, String newPassword)
            throws FirebaseAuthException {
        Optional<Person> personOpt = personRepository.findById(id);
        if (personOpt.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + id + " not found.");
        }

        Person person = personOpt.get();

        // Verify old password before making any changes
        if (!person.getPassword().equals(oldPassword)) {
            return false; // Old password does not match
        }

        boolean changedEmail = false;
        boolean changedPassword = false;

        // Check and update email
        if (newEmail != null && !newEmail.equals(person.getEmailAddress())) {
            person.setEmailAddress(newEmail);
            changedEmail = true;
        }

        // Check and update password
        if (newPassword != null && !newPassword.equals(person.getPassword())) {
            person.setPassword(newPassword);
            changedPassword = true;
        }

        // Update Firebase if needed
        if (changedEmail || changedPassword) {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(person.getFirebaseUid());

            if (changedEmail) {
                request.setEmail(newEmail);
            }
            if (changedPassword) {
                request.setPassword(newPassword);
            }

            FirebaseAuth.getInstance().updateUser(request);
        }

        personRepository.save(person);

        return true;
    }

    @Transactional
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
    public Person createPerson(){
        Person newPerson = new Person();
        Player newPlayer = new Player(newPerson);
        GameOwner newGameOwner = new GameOwner(newPerson);

        newPerson.addRole(newPlayer);
        newPerson.addRole(newGameOwner);

        return personRepository.save(newPerson);
    }

    @Transactional
    public void deletePlayer(int playerId) {
        if (playerRepository.existsById(playerId)) {
            playerRepository.deleteById(playerId);
        }
    }

    @Transactional
    public void deleteGameOwner(int gameOwnerId) {

        if (!gameOwnerRepository.existsById(gameOwnerId)) {
            throw new IllegalArgumentException("Game Owner with ID " + gameOwnerId + " does not exist.");
        }
        gameOwnerRepository.deleteById(gameOwnerId);
    }

    @Transactional
    public void toggleGameOwner(int id) {
        Person person = personRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Person not found with ID: " + id));
        
        List<AccountRole> roles = person.getRoles();
        GameOwner gameOwnerRole = null;
        for (AccountRole role : roles) {
            if (role instanceof GameOwner) {
                gameOwnerRole = (GameOwner) role;
                break;
            }
        }
        if (gameOwnerRole != null) { // if the person has previously been a gameOwner, then just do oposite of isActive
            gameOwnerRole.setActive(!gameOwnerRole.isActive());
        } else { // if a person is toggling and never was a gameOwner then you know they are going from player to gameOwner
            gameOwnerRole = new GameOwner(person);
            gameOwnerRole.setActive(true);
            person.addRole(gameOwnerRole);
        }
        personRepository.save(person); //not sure if it should be here or @transactional does it , because i create a new GameOwner
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
