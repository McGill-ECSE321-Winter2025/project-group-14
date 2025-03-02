package ca.mcgill.ecse321.gamenight.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import ca.mcgill.ecse321.gamenight.dto.PersonResponseDto;
import ca.mcgill.ecse321.gamenight.dto.PlayerResponseDto;
import ca.mcgill.ecse321.gamenight.dto.GameOwnerResponseDto;
import ca.mcgill.ecse321.gamenight.service.UserManagementService;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.GameOwner;

@RestController
public class UserManagementController {

    @Autowired
    private UserManagementService userService;

    /**
     * Create a new Player.
     *
     * @param person The Person entity to convert into a Player.
     * @return The created Player.
     */
    @PostMapping("/players")
    public ResponseEntity<PlayerResponseDto> createPlayer(@RequestBody Person person) {
        Player newPlayer = userService.createPlayer(person);
        return ResponseEntity.ok(new PlayerResponseDto(newPlayer));
    }

    // TODO: Write uodatePLayer

    /**
     * Delete a Player with the given ID.
     *
     * @param id The primary key of the Player to delete.
     * @return empty body
     */
    @DeleteMapping("/players/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable int id) {
        userService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create a new GameOwner.
     *
     * @param person The Person entity to convert into a GameOwner.
     * @return The created GameOwner.
     */
    @PostMapping("/gameowners")
    public ResponseEntity<GameOwnerResponseDto> createOwner(@RequestBody Person person) {
        GameOwner owner = userService.createGameOwner(person);
        return ResponseEntity.ok(new GameOwnerResponseDto(owner));
    }

    /**
     * Delete a GameOwner with the given ID.
     *
     * @param id The primary key of the GameOwner to delete.
     */
    @DeleteMapping("/gameowners/{id}")
    public ResponseEntity<Void> deleteGameOwner(@PathVariable int gameOwnerId) {
        userService.deleteGameOwner(gameOwnerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle the role of a user. If the user is a Player, they become a GameOwner.
     * If they are a GameOwner, they revert to being a Player.
     *
     * @param id The primary key of the Person whose role is being toggled.
     * @return The updated Person.
     */
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<PersonResponseDto> toggleUserType(@PathVariable int id) {
        Person person = userService.toggleUserType(id);
        return ResponseEntity.ok(new PersonResponseDto(person));
    }

    /**
     * Return all users in the system.
     *
     * @return A list of all users.
     */
    @GetMapping("/users")
    public ResponseEntity<List<PersonResponseDto>> getAllUsers() {
        List<Person> users = userService.getAllUsers();
        List<PersonResponseDto> userDtos = users.stream()
                .map(PersonResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Retrieves user details if the authenticated user matches the requested ID.
     *
     * @param id
     * @param request
     * @return A {@link ResponseEntity} with the user's details if authorized,
     *         otherwise a 403 Forbidden response.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetail(@PathVariable int id, HttpServletRequest request) {
        String firebaseUid = (String) request.getAttribute("firebaseUid");
        Person person = userService.getUserById(id);

        // If the requested user is not the authenticated user, deny access
        if (!person.getFirebaseUid().equals(firebaseUid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only view your own profile.");
        }

        return ResponseEntity.ok(new PersonResponseDto(person));
    }

}
