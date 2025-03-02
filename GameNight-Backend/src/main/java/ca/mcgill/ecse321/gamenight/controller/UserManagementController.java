package ca.mcgill.ecse321.gamenight.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.firebase.auth.FirebaseAuthException;

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
     * Updates a user's email or password after verifying their old password.
     * 
     * @param id          User ID.
     * @param newEmail    New email (optional).
     * @param newPassword New password (optional).
     * @param oldPassword Current password for verification.
     * @param request     HTTP request with authenticated Firebase UID.
     * @return {@link ResponseEntity}:
     *         - 200 OK on success.
     *         - 401 Unauthorized if old password is incorrect.
     *         - 403 Forbidden if updating another user's profile.
     *         - 404 Not Found if user doesn't exist.
     *         - 500 Internal Server Error if Firebase update fails.
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable int id,
            @RequestParam(required = false) String newEmail,
            @RequestParam(required = false) String newPassword,
            @RequestParam String oldPassword, // Require old password
            HttpServletRequest request) {

        String firebaseUid = (String) request.getAttribute("firebaseUid");

        Person person = userService.getUserById(id);
        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        // Ensure user is only updating their own profile
        if (!person.getFirebaseUid().equals(firebaseUid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update your own profile.");
        }

        // Attempt to update user (this will check the old password)
        boolean success;
        try {
            success = userService.updateUser(id, oldPassword, newEmail, newPassword);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user in Firebase.");
        }

        return success ? ResponseEntity.ok("User updated successfully.")
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect old password.");
    }

    /**
     * Toggle the role of a user. If they are a Player, they become a GameOwner.
     * If they are a GameOwner, they revert to being a Player.
     *
     * @param id The primary key of the Person whose role is being toggled.
     * @return HTTP 200 if successful, or an error message if failed.
     */
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<String> toggleAccountRole(@PathVariable int id) {
        userService.toggleAccountRole(id);
        return ResponseEntity.ok("User role updated successfully.");
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
