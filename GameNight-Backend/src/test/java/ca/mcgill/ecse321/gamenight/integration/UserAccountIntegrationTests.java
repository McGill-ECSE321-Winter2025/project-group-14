package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.*;

import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.dto.AuthRequest;
import ca.mcgill.ecse321.gamenight.dto.LoginResponse;
import ca.mcgill.ecse321.gamenight.model.Person;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserAccountIntegrationTests {

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

    @AfterEach
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
        AuthRequest request = new AuthRequest();
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

        AuthRequest loginRequest = new AuthRequest();
        loginRequest.setEmailAdress("loginuser@gmail.com");
        loginRequest.setPassword("password123");

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                createURLWithPort("/users/login"),
                loginRequest,
                LoginResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(user.getId(), response.getBody().getUserId());
    }

    @Test
    public void testDeleteUser() {
        Person user = new Person("deleteuser@gmail.com", "password123", "mrUser");
        personRepository.save(user);

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Id", String.valueOf(user.getId()));
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                createURLWithPort("/users/" + user.getId()),
                HttpMethod.DELETE,
                requestEntity,
                Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(personRepository.findById(user.getId()).isPresent());
    }

    @Test
    public void testCreateDuplicateUsername() {
        Person user = new Person("duplicate@gmail.com", "password123", "mrUser");
        personRepository.save(user);

        // Attempt duplicate registration
        AuthRequest request = new AuthRequest();
        request.setEmailAdress("duplicate@gmail.com");
        request.setPassword("differentpassword");
        request.setName("mrUser");

        ResponseEntity<?> response = restTemplate.postForEntity(
                createURLWithPort("/users"),
                request,
                Void.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
}
