package ca.mcgill.ecse321.gamenight.service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.dto.AuthRequestDto;
import ca.mcgill.ecse321.gamenight.exceptions.InvalidCredentialsException;
import ca.mcgill.ecse321.gamenight.exceptions.UsernameTakenException;
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

    public UserManagementService(PersonRepository personRepository,
            PlayerRepository playerRepository,
            GameOwnerRepository gameOwnerRepository) {
        this.personRepository = personRepository;
        this.playerRepository = playerRepository;
        this.gameOwnerRepository = gameOwnerRepository;
    }

    @Transactional
    public void createPerson(AuthRequestDto request) {
        validateEmailAndPassword(
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
    public Person login(AuthRequestDto request) {
        validateEmailAndPassword(
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
        boolean updated = false;
        // Update
        if (newEmail != null && !newEmail.equals(person.getEmailAddress())) {
            person.setEmailAddress(newEmail);
            updated = true;
        }
        if (newPassword != null && !newPassword.isBlank() && !newPassword.equals(person.getPassword())) {
            person.setPassword(newPassword);
            updated = true;
        }

        if (updated)
            personRepository.save(person);

        return true;
    }

    @Transactional
    public void deletePerson(int userId) {
        Person user = personRepository
                .findPersonById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        gameOwnerRepository.delete(gameOwnerRepository.findByPersonId(userId));
        playerRepository.delete(playerRepository.findByPersonId(userId));
        personRepository.delete(user);
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

    private void validateEmailAndPassword(String email, String password) {
        String cleanEmail = StringUtils.trimToNull(email);
        String cleanPassword = StringUtils.trimToNull(password);

        if (cleanEmail == null) {
            throw new IllegalArgumentException("Email adress cannot be empty");
        }
        if (cleanPassword == null) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // pattern from:
        // https://www.geeksforgeeks.org/check-email-address-valid-not-java/
        String pattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern p = Pattern.compile(pattern);
        if (!p.matcher(cleanEmail).matches()) {
            throw new IllegalArgumentException("Invalid email pattern");
        }
    }

}
