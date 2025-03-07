package ca.mcgill.ecse321.gamenight.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.exceptions.InvalidCredentialsException;
import ca.mcgill.ecse321.gamenight.exceptions.UsernameTakenException;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.requests.AuthRequest;
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
    public void createPerson(AuthRequest request) {
        validateUsernameAndPassword(
                request.getEmailAdress(),
                request.getPassword());

        if (personRepository
                .findPersonByEmailAddress(request.getEmailAdress())
                .isPresent()) {
            throw new UsernameTakenException(request.getEmailAdress());
        }

        Person newPerson = new Person(
                request.getEmailAdress(),
                request.getPassword(),
                request.getName());
        Player newPlayer = new Player(newPerson);
        GameOwner newGameOwner = new GameOwner(newPerson);

        personRepository.save(newPerson);
        playerRepository.save(newPlayer);
        gameOwnerRepository.save(newGameOwner);
    }

    @Transactional
    public Person login(AuthRequest request) {
        validateUsernameAndPassword(
                request.getEmailAdress(),
                request.getPassword());

        Person user = personRepository
                .findPersonByEmailAddress(request.getEmailAdress())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.getPassword().equals(request.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    @Transactional
    public boolean updatePerson(int id, String oldPassword, String newEmail, String newPassword) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate old password
        if (!person.getPassword().equals(oldPassword)) {
            return false; // Password mismatch
        }
        // Update
        if (newEmail != null && !newEmail.equals(person.getEmailAddress())) {
            person.setEmailAddress(newEmail);
        }
        if (newPassword != null && !newPassword.isBlank()) {
            person.setPassword(newPassword);
        }

        personRepository.save(person);
        return true;
    }

    @Transactional
    public void deletePerson(int userId) {
        Person user = personRepository
                .findPersonById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        personRepository.delete(user);
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
    public void toggleAccountRole(int id) {
        personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with ID: " + id));

        GameOwner gameOwnerRole = gameOwnerRepository.findById(id).orElse(null);

        gameOwnerRole.setActive(!gameOwnerRole.isActive());

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

    public Player getPlayerById(int playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Person not found with ID: " + playerId));
    }

    private void validateUsernameAndPassword(String username, String password) {
        String cleanUsername = StringUtils.trimToNull(username);
        String cleanPassword = StringUtils.trimToNull(password);

        if (cleanUsername == null) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (cleanPassword == null) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }

}
