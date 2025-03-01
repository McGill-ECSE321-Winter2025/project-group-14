package ca.mcgill.ecse321.gamenight.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

import java.util.List;

import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;

@Service
public class UserManagementService {

    private final PersonRepository personRepository;

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

    public void deletePlayer(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deletePlayer'");
    }

    public GameOwner createGameOwner(Person person) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createGameOwner'");
    }

    public void deleteGameOwner(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteGameOwner'");
    }

    public Person toggleUserType(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toggleUserType'");
    }

    public List<Person> getAllUsers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllUsers'");
    }

    public Person getUserById(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUserById'");
    }

    public Player createPlayer(Person person) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createPlayer'");
    }

}
