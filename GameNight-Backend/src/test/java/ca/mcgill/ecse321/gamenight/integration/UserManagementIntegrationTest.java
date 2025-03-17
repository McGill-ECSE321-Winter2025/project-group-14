package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.service.UserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import ca.mcgill.ecse321.gamenight.controller.UserManagementController;
import ca.mcgill.ecse321.gamenight.dto.AuthRequestDto;
import ca.mcgill.ecse321.gamenight.dto.LoginResponseDto;
import ca.mcgill.ecse321.gamenight.dto.PersonResponseDto;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public class UserManagementIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private GameOwnerRepository gameOwnerRepo;

    @Autowired
    private PlayerRepository playerRepo;


    @Autowired
    private UserManagementService userService;

    @Autowired
    private UserManagementController userController;






    private int testUserId;
    private static final String ORIGINAL_EMAIL = "updateuser@gmail.com";
    private static final String ORIGINAL_PASSWORD = "oldpassword";
    private static final String NEW_EMAIL = "newemail@gmail.com";
    private static final String WRONG_PASSWORD = "wrongpassword";
    private static final String SUCCESS_MESSAGE = "User updated successfully.";
    private static final String ERROR_MESSAGE = "Incorrect old password.";

    @BeforeEach
public void setup() {
    gameOwnerRepo.deleteAll();
    playerRepo.deleteAll();
    personRepository.deleteAll();
    

    Person user = new Person(ORIGINAL_EMAIL, ORIGINAL_PASSWORD, "Test User");
    personRepository.save(user);
    testUserId = user.getId();

    Optional<Person> savedUser = personRepository.findById(testUserId);
    assertTrue(savedUser.isPresent(), "User should be saved in the repository.");

    GameOwner gameOwner = new GameOwner(user);
    gameOwner.setActive(true);
    gameOwnerRepo.save(gameOwner);
}


    @AfterAll
    public void clearDatabase() {
        gameOwnerRepo.deleteAll();
        playerRepo.deleteAll();
        personRepository.deleteAll();
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    @Test
    public void testCreateUserAccount() {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress("newuser@gmail.com");
        request.setPassword("password123");
        request.setName("mrUser");

        ResponseEntity<Void> response = restTemplate.postForEntity(
                createURLWithPort("/users"),
                request,
                Void.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(personRepository.findPersonByEmailAddress("newuser@gmail.com").isPresent());
    }

    @Test
    public void testLoginSuccess() {
        Person user = new Person("loginuser@gmail.com", "password123", "mrUser");
        personRepository.save(user);

        AuthRequestDto loginRequest = new AuthRequestDto();
        loginRequest.setEmailAdress("loginuser@gmail.com");
        loginRequest.setPassword("password123");

        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(
                createURLWithPort("/users/login"),
                loginRequest,
                LoginResponseDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(user.getId(), response.getBody().getUserId());
    }

    @Test
    public void testDeleteUser() {
        Person user = new Person("deleteuser@gmail.com", "password123", "mrUser");
        personRepository.save(user);
        int userId = user.getId();

        GameOwner owner = new GameOwner(user);
        gameOwnerRepo.save(owner);

        Player player = new Player(user);
        playerRepo.save(player);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Id", String.valueOf(userId));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                createURLWithPort("/users/" + userId),
                HttpMethod.DELETE,
                requestEntity,
                Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(personRepository.findById(userId).isPresent());
    }

    @Test
    public void testCreateDuplicateUsername() {
        Person user = new Person("duplicate@gmail.com", "password123", "mrUser");
        personRepository.save(user);

        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress("duplicate@gmail.com");
        request.setPassword("differentpassword");
        request.setName("mrUser");

        ResponseEntity<?> response = restTemplate.postForEntity(
                createURLWithPort("/users"),
                request,
                Void.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testUpdateUserSuccess() {
        String url = createURLWithPort(
                "/users/" + testUserId + "?newEmail=" + NEW_EMAIL + "&oldPassword=" + ORIGINAL_PASSWORD);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(SUCCESS_MESSAGE, response.getBody());
    }

    @Test
    public void testUpdateUserUnauthorized() {
        String url = createURLWithPort(
                "/users/" + testUserId + "?newEmail=" + NEW_EMAIL + "&oldPassword=" + WRONG_PASSWORD);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ERROR_MESSAGE, response.getBody());
    }

    @Test
    public void testToggleRoleGameOwner() {
        Person person = new Person("toggleuser@gmail.com", "password123", "Test User");
        personRepository.save(person);

        GameOwner gameOwner = new GameOwner(person);
        gameOwner.setActive(true);
        gameOwnerRepo.save(gameOwner);

        int userId = gameOwner.getId();
        String url = createURLWithPort("/users/" + userId + "/role");

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        GameOwner updatedOwner = gameOwnerRepo.findById(userId).orElse(null);
        assertNotNull(updatedOwner);
        assertFalse(updatedOwner.isActive());
    }


    @Test
    public void testGetUserById() {
        gameOwnerRepo.deleteAll();
        playerRepo.deleteAll();
        personRepository.deleteAll();

        Person testPerson = new Person("authtestuser@example.com", "password123", "Auth Test User");
        personRepository.save(testPerson);
        int personId = testPerson.getId();

        GameOwner gameOwner = new GameOwner(testPerson);
        gameOwner.setActive(true);
        gameOwnerRepo.save(gameOwner);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Id", String.valueOf(personId));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/users/" + personId),
                HttpMethod.GET,
                requestEntity,
                String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Expected Unauthorized error.");
            assertTrue(response.getBody().contains("Unauthorized"), "Expected 'Unauthorized' message.");
        } else {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                PersonResponseDto returnedPerson = objectMapper.readValue(response.getBody(), PersonResponseDto.class);

                assertNotNull(returnedPerson);
                assertEquals(personId, returnedPerson.getPersonId());
                assertEquals("authtestuser@example.com", returnedPerson.getEmail());
                assertEquals("Auth Test User", returnedPerson.getName());
            } catch (Exception e) {
                fail("Failed to parse JSON response: " + e.getMessage());
            }
        }
    }
   
    @Test
    public void testGetUserByIdNotFound() {
        // Create a test user for authentication
        Person user = new Person("authtestuser@example.com", "password123", "Auth Test User");
        personRepository.save(user);
        int userId = user.getId();
        
        // Add a role for this user
        GameOwner gameOwner = new GameOwner(user);
        gameOwner.setActive(true);
        gameOwnerRepo.save(gameOwner);
        
        int nonExistentUserId = 99999;
        
        assertFalse(personRepository.findById(nonExistentUserId).isPresent());
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Id", String.valueOf(userId));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/users/" + nonExistentUserId),
                HttpMethod.GET,
                requestEntity,
                String.class);
    
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetAllUsers() {
        gameOwnerRepo.deleteAll();
        playerRepo.deleteAll();
        personRepository.deleteAll();
        
        // Create test users
        Person user1 = new Person("getalluser1@example.com", "password123", "Get All User One");
        personRepository.save(user1);
        int user1Id = user1.getId();
        
        // Add a role for this user
        GameOwner gameOwner = new GameOwner(user1);
        gameOwner.setActive(true);
        gameOwnerRepo.save(gameOwner);
        
        Person user2 = new Person("getalluser2@example.com", "password456", "Get All User Two");
        personRepository.save(user2);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Id", String.valueOf(user1Id));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        
        ResponseEntity<String> responseString = restTemplate.exchange(
                createURLWithPort("/users"),
                HttpMethod.GET,
                requestEntity,
                String.class);

        ResponseEntity<PersonResponseDto[]> response = restTemplate.exchange(
                createURLWithPort("/users"),
                HttpMethod.GET,
                requestEntity,
                PersonResponseDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        PersonResponseDto[] users = response.getBody();
        assertTrue(users.length >= 2);
        
        boolean foundUser1 = false;
        boolean foundUser2 = false;
        
        for (PersonResponseDto user : users) {
            if (user.getEmail().equals("getalluser1@example.com")) {
                foundUser1 = true;
                assertEquals("Get All User One", user.getName());
            } else if (user.getEmail().equals("getalluser2@example.com")) {
                foundUser2 = true;
                assertEquals("Get All User Two", user.getName());
            }
        }
        
        assertTrue(foundUser1, "User One should be in the response");
        assertTrue(foundUser2, "User Two should be in the response");
    }
    @Test
    public void testGetUserDetail_UserNotFound() {
        Person authUser = new Person("authuser@example.com", "passA", "Authenticated User");
        personRepository.save(authUser);

        int nonExistentUserId = 99999;
        assertFalse(personRepository.findById(nonExistentUserId).isPresent());

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Id", String.valueOf(authUser.getId()));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/users/" + nonExistentUserId),
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody());
    }
    
    
    @Test
    public void testGetUserDetail_UnauthorizedAccess() {
        Person userA = new Person("userA@example.com", "passA", "User A");
        personRepository.save(userA);
        Person userB = new Person("userB@example.com", "passB", "User B");
        personRepository.save(userB);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Id", String.valueOf(userA.getId()));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort("/users/" + userB.getId()),
            HttpMethod.GET,
            requestEntity,
            String.class
        );
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            assertEquals("You can only view your own profile.", response.getBody());
    }
    
    @Test
    public void testGetUserDetail_NoHeader() {
        int someUserId = 123;
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort("/users/" + someUserId),
            HttpMethod.GET,
            requestEntity,
            String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("No valid authentication.", response.getBody());
    }
    @Test
    public void testGetUserDetail_UserIsNull() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Id", "999");
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort("/users/999"),
            HttpMethod.GET,
            requestEntity,
            String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody());
    }

    @Test
    void testGetUserDetail_nonExistentUser_returns404() {
        Person authUser = new Person("authUser@example.com", "somePassword", "Auth User");
        personRepository.save(authUser);
        int authUserId = authUser.getId();

        int nonExistentUserId = 9999;
        assertFalse(personRepository.findById(nonExistentUserId).isPresent(),
                "No user should exist with ID " + nonExistentUserId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Id", String.valueOf(authUserId));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/users/" + nonExistentUserId),
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), 
                "Expected 404 when requesting a non-existent user.");
        assertEquals("User not found.", response.getBody(), 
                "Expected 'User not found.' in the response body.");
    }
    @Test
    public void testGetUserById_notFound_returns404() {
        int nonExistentUserId = 9999;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Id", "123");
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort("/users/" + nonExistentUserId),
            HttpMethod.GET,
            requestEntity,
            String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody());
    }


}



