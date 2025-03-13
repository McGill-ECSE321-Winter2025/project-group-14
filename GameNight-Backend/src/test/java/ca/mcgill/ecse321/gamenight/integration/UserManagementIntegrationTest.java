package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.*;

import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;

import ca.mcgill.ecse321.gamenight.dto.AuthRequestDto;
import ca.mcgill.ecse321.gamenight.dto.LoginResponseDto;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

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

        // Create and save test user
        Person user = new Person(ORIGINAL_EMAIL, ORIGINAL_PASSWORD, "Test User");
        personRepository.save(user);
        testUserId = user.getId();
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

    @SuppressWarnings("null")
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

        // Attempt duplicate registration
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
}
