package ca.mcgill.ecse321.gamenight.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import ca.mcgill.ecse321.gamenight.dto.AuthRequestDto;
import ca.mcgill.ecse321.gamenight.dto.LoginResponseDto;
import ca.mcgill.ecse321.gamenight.dto.PersonResponseDto;
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
     * @param userId
     * @return a ResponseEntity with HTTP status 200 (OK)
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
     * @param id
     * @param newEmail
     * @param newPassword
     * @param oldPassword
     * @return A {@link ResponseEntity}:
     *         - 200 OK if the update is successful.
     *         - 401 Unauthorized if the old password is incorrect.
     *         - 404 Not Found if the user does not exist.
     *         - 400 Bad Request if no new values are provided.
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
     * @param id The primary key of the Person whose role is being toggled.
     * @return HTTP 200 if successful, or an error message if failed.
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<String> toggleAccountRole(@PathVariable int id) {
        try {
            userService.toggleAccountRole(id);
            return ResponseEntity.ok("User role updated successfully.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
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
     * @param id
     * @param request
     * @return A {@link ResponseEntity} with the user's details if authorized,
     *         otherwise a 403 Forbidden or 404 Not Found response.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetail(@PathVariable int id, HttpServletRequest request) {
        Integer authenticatedUserId = (Integer) request.getAttribute("userId");

        if (authenticatedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: No valid authentication.");
        }

        Person person = userService.getUserById(id);
        if (person == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        if (!authenticatedUserId.equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only view your own profile.");
        }

        return ResponseEntity.ok(new PersonResponseDto(person));
    }
}
