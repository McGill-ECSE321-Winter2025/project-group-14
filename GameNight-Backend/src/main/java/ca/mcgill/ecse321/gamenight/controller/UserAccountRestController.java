package ca.mcgill.ecse321.gamenight.controller;

import ca.mcgill.ecse321.gamenight.middleware.RequireUser;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.requests.AuthRequest;
import ca.mcgill.ecse321.gamenight.responses.LoginResponse;
import ca.mcgill.ecse321.gamenight.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserAccountRestController {

    private final UserManagementService userManagementService;

    @Autowired
    public UserAccountRestController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @PostMapping
    public ResponseEntity<?> createUserAccount(
            @RequestBody AuthRequest request) {
        userManagementService.createPerson(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{userId}")
    @RequireUser
    public ResponseEntity<?> deleteUserAccount(@PathVariable int userId) {
        userManagementService.deletePerson(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody AuthRequest request) {
        Person user = userManagementService.login(request);
        return ResponseEntity.ok(
                new LoginResponse(user.getId(), user.getEmailAddress()));
    }
}
