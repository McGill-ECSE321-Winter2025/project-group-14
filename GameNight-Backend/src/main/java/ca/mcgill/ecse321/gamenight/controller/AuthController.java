package ca.mcgill.ecse321.gamenight.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gamenight.dto.PersonResponseDto;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.service.UserManagementService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserManagementService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Person person) {
        try {
            Person newUser = userService.registerUser(person.getEmailAddress(), person.getPassword(), person.getName());
            return ResponseEntity.ok(new PersonResponseDto(newUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Person person) {
        try {
            String firebaseUid = userService.loginUser(person.getEmailAddress());
            Person foundPerson = userService.getUserByFirebaseUid(firebaseUid);
            return foundPerson != null
                    ? ResponseEntity.ok(new PersonResponseDto(foundPerson))
                    : ResponseEntity.status(404).body("User not found in local database.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String idToken) {
        try {
            String firebaseUid = userService.verifyFirebaseToken(idToken);
            userService.deleteUser(firebaseUid);
            return ResponseEntity.ok("User deleted.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
