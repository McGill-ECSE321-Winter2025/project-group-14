package ca.mcgill.ecse321.gamenight.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import ca.mcgill.ecse321.gamenight.dto.AuthRequestDto;
import ca.mcgill.ecse321.gamenight.dto.LoginResponseDto;
import ca.mcgill.ecse321.gamenight.dto.PersonResponseDto;
import ca.mcgill.ecse321.gamenight.exception.ForbiddenException;
import ca.mcgill.ecse321.gamenight.exception.InvalidInputException;
import ca.mcgill.ecse321.gamenight.exception.UnauthorizedException;
import ca.mcgill.ecse321.gamenight.exception.UniquenessConstaintException;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.middleware.RequireUser;
import ca.mcgill.ecse321.gamenight.service.UserManagementService;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;

@RestController
public class UserManagementController {

    @Autowired
    private UserManagementService userService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userService = userManagementService;
    }

    /**
     * Creates a new user in the application. The GameOwner and Player roles are
     * also initialized.
     *
     * @param request The authentication request containing user details.
     * @return A ResponseEntity with HTTP status 201 (Created) if successful.
     * @throws UniquenessConstaintException if the email is already in use.
     * @throws InvalidInputException        if the email or password is invalid.
     */
    @PostMapping("/users")
    public ResponseEntity<?> createPerson(
            @RequestBody AuthRequestDto request) {
        userService.createPerson(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Deletes a user by ID, removing associated GameOwner and Player roles.
     *
     * @param id The ID of the user to delete.
     * @return A ResponseEntity with HTTP status 200 (OK) if successful.
     * @throws ObjectNotFoundException if the user does not exist.
     */
    @DeleteMapping("/users/{id}")
    @RequireUser
    public ResponseEntity<?> deletePerson(@PathVariable int id) {
        userService.deletePerson(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Authenticates a user and returns login response containing user details.
     *
     * @param request The authentication request containing login credentials.
     * @return A ResponseEntity containing the login response with user ID and
     *         email.
     * @throws InvalidInputException if the username or password is incorrect.
     */
    @PostMapping("/users/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody AuthRequestDto request) {
        Person user = userService.login(request);
        LoginResponseDto response = new LoginResponseDto(user.getId(), user.getEmailAddress());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a user's email or password after verifying their old password.
     *
     * @param id          The ID of the user to update.
     * @param newEmail    The new email (optional).
     * @param newPassword The new password (optional).
     * @param oldPassword The user's current password for verification.
     * @return A ResponseEntity with a success message if the update is successful.
     * @throws UnauthorizedException   if the provided old password is incorrect.
     * @throws ObjectNotFoundException if the user does not exist.
     * @throws InvalidInputException   if no new values are provided for update.
     */
    @PutMapping("/users/{id}")
    @RequireUser
    public ResponseEntity<?> updateUser(
            @PathVariable int id,
            @RequestParam(required = false) String newEmail,
            @RequestParam(required = false) String newPassword,
            @RequestParam String oldPassword) {

        boolean success = userService.updatePerson(id, oldPassword, newEmail, newPassword);

        if (!success) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect old password.");
        }

        return ResponseEntity.ok("User updated successfully.");
    }

    /**
     * Toggles the role of a user between Player and GameOwner.
     *
     * @param id The ID of the user whose role is being toggled.
     * @return A ResponseEntity with a success message.
     * @throws ObjectNotFoundException if the user does not exist.
     */
    @PutMapping("/users/{id}/role")
    @RequireUser
    public ResponseEntity<?> toggleAccountRole(@PathVariable int id) {
        userService.toggleAccountRole(id);
        return ResponseEntity.ok("Role toggled successfully.");

    }

    /**
     * Retrieves a list of all users in the system.
     *
     * @return A ResponseEntity containing a list of all users.
     */
    @GetMapping("/users")
    @RequireUser
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
     * @param id      The ID of the user to retrieve.
     * @param request The HTTP request containing authentication headers.
     * @return A ResponseEntity with the user's details.
     * @throws UnauthorizedException   if no authentication is provided.
     * @throws ObjectNotFoundException if the user does not exist.
     * @throws ForbiddenException      if the authenticated user attempts to view
     *                                 another user's profile.
     */
    @GetMapping("/users/{id}")
    @RequireUser
    public ResponseEntity<?> getUserDetail(@PathVariable int id, HttpServletRequest request) {
        String headerUserId = request.getHeader("User-Id");

        Person authUser = userService.getUserById(Integer.parseInt(headerUserId));
        Person targetUser = userService.getUserById(id);

        if (authUser.getId() != id) {
            throw new ForbiddenException("You can only view your own profile.");
        }

        return ResponseEntity.ok(new PersonResponseDto(targetUser));
    }

    @GetMapping("/users/{userId}/owner-id")
    public ResponseEntity<Integer> getOwnerIdByUserId(@PathVariable Integer userId) {
        Integer ownerId = userService.findOwnerIdByUserId(userId);
        return ResponseEntity.ok(ownerId);
    }

    /**
     * Get the player id for the given person
     * 
     * @param personId Id of the person
     * @return The id of the player
     */
    @GetMapping("/users/{userId}/player-id")
    public Integer getPlayerByPersonId(@PathVariable Integer userId) {
        Player player = userService.getPlayerByPersonId(userId);
        return player.getId();
    }

    /**
     * Get the owner id for the given person
     * 
     * @param personId Id of the person
     * @return The id of the game owner
     */
    @GetMapping("/game-owners")
    public String getGameOwnerByPersonId(@RequestParam(name = "person_id") int personId) {
        return userService.getGameOwnerByPersonId(personId).getPerson().getName();
    }

    /**
     * Returns whether the user is an active game owner.
     *
     * @param userId the user id
     * @return true if the user is an active owner, false otherwise
     */
    @GetMapping("/users/{userId}/is-owner")
    @RequireUser
    public ResponseEntity<Boolean> isActiveOwner(@PathVariable int userId) {
        return ResponseEntity.ok(userService.isActiveOwner(userId));
    }

    @PutMapping("/users/{id}/username")
    @RequireUser
    public ResponseEntity<?> updateUsername(
            @PathVariable int id,
            @RequestParam String newUsername,
            HttpServletRequest request) {
    
        String headerUserId = request.getHeader("User-Id");
    
        if (headerUserId == null || Integer.parseInt(headerUserId) != id) {
            throw new ForbiddenException("You can only update your own username.");
        }
    
        userService.updateUsername(id, newUsername);
        return ResponseEntity.ok("Username updated successfully.");
    }
    
    /**
     * Get the player ID for the given person ID (user ID)
     * 
     * Example: GET /users/33/player-id
     */
    @GetMapping("/users/{personId}/player-id")
    public ResponseEntity<Integer> getPlayerIdFromPersonId(@PathVariable int personId) {
        Player player = userService.getPlayerByPersonId(personId);
        return ResponseEntity.ok(player.getId());
    }

}