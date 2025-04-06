package ca.mcgill.ecse321.gamenight.service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ca.mcgill.ecse321.gamenight.dto.AuthRequestDto;
import ca.mcgill.ecse321.gamenight.exception.InvalidInputException;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.exception.UniquenessConstaintException;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.Registration;
import ca.mcgill.ecse321.gamenight.repo.BorrowingRequestRepository;
import ca.mcgill.ecse321.gamenight.repo.EventRepository;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.GameReviewRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.repo.RegistrationRepository;
import ca.mcgill.ecse321.gamenight.repo.ScheduledGameRepository;
import jakarta.transaction.Transactional;

@Service
public class UserManagementService {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private GameOwnerRepository gameOwnerRepository;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private BorrowingRequestRepository borrowingRequestRepository;

    @Autowired
    private GameReviewRepository gameReviewRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ScheduledGameRepository scheduledGameRepository;

    @Transactional
    public void createPerson(AuthRequestDto request) {
        validateEmailAndPassword(
                request.getEmailAdress(),
                request.getPassword());

        if (personRepository
                .findPersonByEmailAddress(request.getEmailAdress())
                .isPresent()) {
            throw new UniquenessConstaintException(request.getEmailAdress());
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
                .orElseThrow(() -> new InvalidInputException("Invalid username or password"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new InvalidInputException("Invalid username or password");
        }

        return user;
    }

    @Transactional
    public boolean updatePerson(int id, String oldPassword, String newEmail, String newPassword) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));

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
        Person person = personRepository.findPersonById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("User not found"));

        // -- Remove Events this person hosted (must come FIRST) --
        // This MUST happen before deleting player and registrations
        eventRepository.findAll().forEach(event -> {
            Registration earliest = registrationRepository.findFirstByKey_EventIdOrderByCreatedAtAsc(event.getId());
            if (earliest != null && earliest.getKey().getPlayer().getPerson().getId() == userId) {
                // Delete all registrations for this event
                registrationRepository.findByKey_EventId(event.getId())
                        .forEach(registrationRepository::delete);

                // Delete scheduled games in this event
                scheduledGameRepository.findByKey_EventId(event.getId())
                        .forEach(scheduledGameRepository::delete);

                // Delete event
                eventRepository.delete(event);
            }
        });

        // -- Remove Player-based links --
        Player player = playerRepository.findByPersonId(userId);
        if (player != null) {
            // Delete all remaining registrations
            registrationRepository.findByKey_PlayerId(player.getId())
                    .forEach(registrationRepository::delete);

            // Delete reviews made by this player
            gameReviewRepository.findByReviewer(player)
                    .forEach(gameReviewRepository::delete);

            // Delete borrowing requests made by this player
            borrowingRequestRepository.findBySender(player)
                    .forEach(borrowingRequestRepository::delete);

            playerRepository.delete(player);
        }

        // -- Remove GameOwner-based links --
        GameOwner owner = gameOwnerRepository.findByPersonId(userId);
        if (owner != null) {
            gameCopyRepository.findByGameOwner(owner).forEach(copy -> {
                borrowingRequestRepository.findByGameCopy(copy)
                        .forEach(borrowingRequestRepository::delete);
                gameCopyRepository.delete(copy);
            });
            gameOwnerRepository.delete(owner);
        }

        // -- Delete Person --
        personRepository.delete(person);
    }

    @Transactional
    public void toggleAccountRole(int id) {
        GameOwner owner = gameOwnerRepository.findByPersonId(id);
        if (owner == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GameOwner not found with ID: " + id);
        }
        owner.setActive(!owner.isActive());
        gameOwnerRepository.save(owner);
    }

    @Transactional
    public void updateUsername(int id, String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new InvalidInputException("Username cannot be empty.");
        }

        Person person = getUserById(id);
        person.setName(newUsername);
        personRepository.save(person);
    }

    public List<Person> getAllUsers() {
        Iterable<Person> iterable = personRepository.findAll();
        return StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());
    }

    public Person getUserById(int userId) {
        return personRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found."));
    }

    public Player getPlayerById(int playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new ObjectNotFoundException("Person not found with ID: " + playerId));
    }

    private void validateEmailAndPassword(String email, String password) {
        String cleanEmail = StringUtils.trimToNull(email);
        String cleanPassword = StringUtils.trimToNull(password);

        if (cleanEmail == null) {
            throw new InvalidInputException("Email adress cannot be empty");
        }
        if (cleanPassword == null) {
            throw new InvalidInputException("Password cannot be empty");
        }

        // pattern from:
        // https://www.geeksforgeeks.org/check-email-address-valid-not-java/
        String pattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern p = Pattern.compile(pattern);
        if (!p.matcher(cleanEmail).matches()) {
            throw new InvalidInputException("Invalid email pattern");
        }
    }

    public Integer findOwnerIdByUserId(Integer userId) {
        // Find the GameOwner associated with this user
        GameOwner owner = gameOwnerRepository.findByPersonId(userId);
        return owner.getId();
    }

    public Player getPlayerByPersonId(int personId) {
        return playerRepository.findByPersonId(personId);
    }

    public GameOwner getGameOwnerByPersonId(int personId) {
        return gameOwnerRepository.findByPersonId(personId);
    }

    public boolean isActiveOwner(int userId) {
        GameOwner owner = getGameOwnerByPersonId(userId);
        return owner != null && owner.isActive();
    }

}
