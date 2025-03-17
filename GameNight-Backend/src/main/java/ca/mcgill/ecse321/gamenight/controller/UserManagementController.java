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
import ca.mcgill.ecse321.gamenight.exceptions.ForbiddenAccessException;
import ca.mcgill.ecse321.gamenight.exceptions.GameOwnerNotFoundException;
import ca.mcgill.ecse321.gamenight.exceptions.UnauthedException;
import ca.mcgill.ecse321.gamenight.exceptions.UserNotFoundException;
import ca.mcgill.ecse321.gamenight.middleware.RequireUser;
import ca.mcgill.ecse321.gamenight.service.UserManagementService;
import ca.mcgill.ecse321.gamenight.model.Person;

@RestController
public class UserManagementController {

    @Autowired
    private UserManagementService userService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userService = userManagementService;
    }

    // tested
    /**
     * Creates a new user. The GameOwner and Player are also generated.
     * 
     * @param request
     * @return a ResponseEntity with HTTP status 201 (Created)
     */
    @PostMapping("/users")
    public ResponseEntity<?> createPerson(
            @RequestBody AuthRequestDto request) {
        userService.createPerson(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // tested
    /**
     * Deletes a Person by ID. The GameOwner and Player are also deleted.
     * 
     * @param userId The ID of the user to delete.
     * @return A ResponseEntity with HTTP 200 OK if the deletion is successful.
     * @throws UserNotFoundException if the user does not exist.
     */
    @DeleteMapping("/users/{userId}")
    @RequireUser
    public ResponseEntity<?> deletePerson(@PathVariable int userId) {
        userService.deletePerson(userId);
        return ResponseEntity.ok().build();
    }

    // tested
    /**
     * Authenticates a user and returns login response.
     * 
     * @param request
     * @return a ResponseEntity containing the login response with user ID and email
     */
    @PostMapping("/users/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody AuthRequestDto request) {
        Person user = userService.login(request);
        LoginResponseDto response = new LoginResponseDto(user.getId(), user.getEmailAddress());
        return ResponseEntity.ok(response);
    }

    


    // tested
    /**
     * Updates a user's email or password after verifying their old password.
     *
     * @param id The ID of the user to update.
     * @param newEmail The new email (optional).
     * @param newPassword The new password (optional).
     * @param oldPassword The user's current password for verification.
     * @return A ResponseEntity with a success message if the update is successful.
     * @throws UnauthedException if the provided old password is incorrect.
     * @throws UserNotFoundException if the user does not exist.
     * @throws BadRequestException if no new values are provided for update.
    */
    @PutMapping("/users/{id}")
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

    // tested
    /**
     * Toggle the role of a user. If they are a Player, they become a GameOwner.
     * If they are a GameOwner, they revert to being a Player.
     *
     * @param id The ID of the Person whose role is being toggled.
     * @return A ResponseEntity with a success message.
     * @throws GameOwnerNotFoundException if the user does not have a GameOwner role.
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> toggleAccountRole(@PathVariable int id) {
        userService.toggleAccountRole(id);
        return ResponseEntity.ok("Role toggled successfully.");
        
    }
    

    // tested
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

    // tested
    /**
     * Retrieves user details if the authenticated user matches the requested ID.
     *
     * @param id The ID of the user to retrieve.
     * @param request The HTTP request containing authentication headers.
     * @return A ResponseEntity with the user's details.
     * @throws UnauthedException if no authentication is provided.
     * @throws UserNotFoundException if the user does not exist.
     * @throws ForbiddenAccessException if the authenticated user attempts to view another user's profile.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetail(@PathVariable int id, HttpServletRequest request) {
      
        String headerUserId = request.getHeader("User-Id");
        if (headerUserId == null) {
            throw new UnauthedException("No valid authentication.");

        }

        Person authUser = userService.getUserById(Integer.parseInt(headerUserId)); 
        Person targetUser = userService.getUserById(id);

        if (authUser.getId() != id) {
            throw new ForbiddenAccessException("You can only view your own profile.");

        }

        return ResponseEntity.ok(new PersonResponseDto(targetUser));

       
    }
}