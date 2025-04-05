package ca.mcgill.ecse321.gamenight.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.repo.BorrowingRequestRepository;
import ca.mcgill.ecse321.gamenight.repo.EventRepository;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.GameReviewRepository;
import ca.mcgill.ecse321.gamenight.repo.RegistrationRepository;
import ca.mcgill.ecse321.gamenight.repo.ScheduledGameRepository;


import ca.mcgill.ecse321.gamenight.dto.AuthRequestDto;
import ca.mcgill.ecse321.gamenight.dto.LoginResponseDto;
import ca.mcgill.ecse321.gamenight.dto.PersonResponseDto;

import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.Event;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameReview;
import ca.mcgill.ecse321.gamenight.model.Registration;
import ca.mcgill.ecse321.gamenight.model.ScheduledGame;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class UserManagementIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired private PersonRepository personRepository;
    @Autowired private GameOwnerRepository gameOwnerRepo;
    @Autowired private PlayerRepository playerRepo;
    @Autowired private BorrowingRequestRepository borrowingRequestRepository;
    @Autowired private GameCopyRepository gameCopyRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;
    @Autowired private GameReviewRepository gameReviewRepository;
    @Autowired private ScheduledGameRepository scheduledGameRepository;


    @Autowired
    private ObjectMapper objectMapper;

    private int testUserId;
    private Person setupPerson;
    private static final String ORIGINAL_EMAIL = "updateuser_setup@gmail.com";
    private static final String ORIGINAL_PASSWORD = "oldpassword";
    private static final String NEW_EMAIL = "newemail@gmail.com";
    private static final String NEW_PASSWORD = "newpassword";
    private static final String WRONG_PASSWORD = "wrongpassword";
    private static final String SUCCESS_MESSAGE = "User updated successfully.";
    private static final String ERROR_MESSAGE = "Incorrect old password.";

    @BeforeEach
    public void setup() {
        Person user = new Person(ORIGINAL_EMAIL, ORIGINAL_PASSWORD, "Setup User");
        setupPerson = personRepository.save(user);
        testUserId = setupPerson.getId();

        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(setupPerson);
        gameOwner.setActive(true);
        gameOwnerRepo.save(gameOwner);

        Player player = new Player();
        player.setPerson(setupPerson);
        playerRepo.save(player);
        }


    @Test
    public void testCreateUserAccount() throws Exception {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress("newuser@gmail.com");
        request.setPassword("password123");
        request.setName("mrUser");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated());

        assertTrue(personRepository.findPersonByEmailAddress("newuser@gmail.com").isPresent());
    }

    @Test
    public void testLoginSuccess() throws Exception {
        String email = "loginuser_test@gmail.com";
        String password = "password123";
        Person user = new Person(email, password, "Login Test User");
        personRepository.save(user);

        AuthRequestDto loginRequest = new AuthRequestDto();
        loginRequest.setEmailAdress(email);
        loginRequest.setPassword(password);

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    @Test
    public void testDeleteUser() throws Exception {
        Person user = new Person("deleteuser_test@gmail.com", "password123", "Delete Test User");
        Person savedUser = personRepository.save(user);
        int userId = savedUser.getId();

        GameOwner owner = new GameOwner(); owner.setPerson(savedUser); gameOwnerRepo.save(owner);
        Player player = new Player(); player.setPerson(savedUser); playerRepo.save(player);

        mockMvc.perform(delete("/users/{id}", userId)
                .header("User-Id", String.valueOf(userId)))
               .andExpect(status().isOk());

        assertFalse(personRepository.findById(userId).isPresent());
        assertNull(gameOwnerRepo.findByPersonId(userId));
        assertNull(playerRepo.findByPersonId(userId));
    }

    @Test
    public void testCreateDuplicateUsername() throws Exception {
        personRepository.save(new Person("duplicate_test@gmail.com", "password123", "Duplicate User"));

        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress("duplicate_test@gmail.com");
        request.setPassword("differentpassword");
        request.setName("mrUser");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isConflict());
    }

    @Test
    public void testUpdateUserSuccess() throws Exception {
        mockMvc.perform(put("/users/{id}", testUserId)
                .header("User-Id", String.valueOf(testUserId))
                .param("newEmail", NEW_EMAIL)
                .param("oldPassword", ORIGINAL_PASSWORD)
                )
               .andExpect(status().isOk())
               .andExpect(content().string(SUCCESS_MESSAGE));

        Optional<Person> updatedPerson = personRepository.findById(testUserId);
        assertTrue(updatedPerson.isPresent());
        assertEquals(NEW_EMAIL, updatedPerson.get().getEmailAddress());
    }

    @Test
    public void testUpdateUserUnauthorized_WrongPassword() throws Exception {
        mockMvc.perform(put("/users/{id}", testUserId)
                .header("User-Id", String.valueOf(testUserId))
                .param("newEmail", NEW_EMAIL)
                .param("oldPassword", WRONG_PASSWORD)
                )
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.errors[0]").value(ERROR_MESSAGE));
    }

    @Test
    public void testUpdateUserUnauthorized_WrongUser() throws Exception {
        Person anotherUser = new Person("another@test.com", "pw", "Another");
        personRepository.save(anotherUser);

        mockMvc.perform(put("/users/{id}", testUserId)
                .header("User-Id", String.valueOf(anotherUser.getId()))
                .param("newEmail", NEW_EMAIL)
                .param("oldPassword", ORIGINAL_PASSWORD)
                )
               
               .andExpect(status().isForbidden());
    }

    @Test
    public void testToggleRoleGameOwner() throws Exception {
        Person person = new Person("toggleuser_test@gmail.com", "password123", "Toggle User");
        Person savedPerson = personRepository.save(person);
        int userId = savedPerson.getId();

        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(savedPerson);
        gameOwner.setActive(true);
        gameOwnerRepo.save(gameOwner);

        Player player = new Player();
        player.setPerson(savedPerson);
        playerRepo.save(player);

        mockMvc.perform(put("/users/{id}/role", userId)
                .header("User-Id", String.valueOf(userId)))
               .andExpect(status().isOk());

        GameOwner updatedOwner = gameOwnerRepo.findByPersonId(userId);
        assertNotNull(updatedOwner);
        assertFalse(updatedOwner.isActive(), "GameOwner should be inactive after toggle");

        Player updatedPlayer = playerRepo.findByPersonId(userId);
        assertNotNull(updatedPlayer, "Player role should exist after toggle");
    }

    @Test
    public void testGetUserById() throws Exception {
        String email = "getbyid_test@example.com";
        Person testPerson = new Person(email, "password123", "GetById Test User");
        Person savedPerson = personRepository.save(testPerson);
        int personId = savedPerson.getId();

        GameOwner gameOwner = new GameOwner();
        gameOwner.setPerson(savedPerson);
        gameOwner.setActive(true);
        gameOwnerRepo.save(gameOwner);

        mockMvc.perform(get("/users/{id}", personId)
                .header("User-Id", String.valueOf(personId)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.personId").value(personId))
               .andExpect(jsonPath("$.email").value(email))
               .andExpect(jsonPath("$.name").value("GetById Test User"));
    }

    @Test
    public void testGetUserByIdNotFound() throws Exception {
        int nonExistentUserId = 99999;
        assertFalse(personRepository.findById(nonExistentUserId).isPresent());

        mockMvc.perform(get("/users/{id}", nonExistentUserId)
                .header("User-Id", String.valueOf(testUserId)))
               .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserById_notFound_returns404() throws Exception {
        int nonExistentUserId = 9998;
        assertFalse(personRepository.findById(nonExistentUserId).isPresent());

        mockMvc.perform(get("/users/{id}", nonExistentUserId)
                .header("User-Id", String.valueOf(testUserId)))
               .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllUsers() throws Exception {
        Person user1 = new Person("getalluser1_test@example.com", "password123", "Get All User One");
        Person savedUser1 = personRepository.save(user1);
        GameOwner go1 = new GameOwner(); go1.setPerson(savedUser1); gameOwnerRepo.save(go1);

        Person user2 = new Person("getalluser2_test@example.com", "password456", "Get All User Two");
        Person savedUser2 = personRepository.save(user2);
        Player p2 = new Player(); p2.setPerson(savedUser2); playerRepo.save(p2);

        MvcResult result = mockMvc.perform(get("/users")
                                   .header("User-Id", String.valueOf(testUserId)))
                                  .andExpect(status().isOk())
                                  .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        PersonResponseDto[] users = objectMapper.readValue(jsonResponse, PersonResponseDto[].class);

        boolean foundUser1 = false;
        boolean foundUser2 = false;
        boolean foundSetupUser = false;
        for (PersonResponseDto user : users) {
            if (user.getEmail().equals(savedUser1.getEmailAddress())) foundUser1 = true;
            if (user.getEmail().equals(savedUser2.getEmailAddress())) foundUser2 = true;
            if (user.getEmail().equals(setupPerson.getEmailAddress())) foundSetupUser = true;
        }
        assertTrue(foundUser1, "User One should be in the response");
        assertTrue(foundUser2, "User Two should be in the response");
        assertTrue(foundSetupUser, "Setup User should be in the response");
        assertTrue(users.length >= 3, "Should find at least 3 users");
    }

    @Test
    public void testGetUserDetail_UserNotFound() throws Exception {
        int nonExistentUserId = 99997;
        mockMvc.perform(get("/users/{id}", nonExistentUserId)
                .header("User-Id", String.valueOf(testUserId)))
               .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserDetail_UnauthorizedAccess() throws Exception {
        Person userA = new Person("userA_detail@example.com", "passA", "User A Detail");
        personRepository.save(userA);
        Person userB = new Person("userB_detail@example.com", "passB", "User B Detail");
        personRepository.save(userB);

        mockMvc.perform(get("/users/{id}", userB.getId())
                .header("User-Id", String.valueOf(userA.getId())))
               .andExpect(status().isForbidden());
    }

    @Test
    public void testGetUserDetail_NoIdInHeader() throws Exception {
        mockMvc.perform(get("/users/{id}", testUserId))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserDetail_UserIsNull() throws Exception {
        int nonExistentUserId = 99996;
        mockMvc.perform(get("/users/{id}", nonExistentUserId)
                .header("User-Id", String.valueOf(testUserId)))
               .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserDetail_the_headerUserId_in_not_integer() throws Exception {
        mockMvc.perform(get("/users/{id}", testUserId)
                .header("User-Id", "not-an-integer"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserDetail_the_header_doesnt_exist() throws Exception {
         int nonExistentAuthUserId = 99995;
        mockMvc.perform(get("/users/{id}", testUserId)
                .header("User-Id", String.valueOf(nonExistentAuthUserId)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetPlayerByPersonId() throws Exception {
        Player player = playerRepo.findByPersonId(testUserId);
        assertNotNull(player, "Player role should exist for setup user from @BeforeEach");
        int testPlayerId = player.getId();

        mockMvc.perform(get("/players")
                .param("person_id", String.valueOf(testUserId))
                .header("User-Id", String.valueOf(testUserId)))
               .andExpect(status().isOk())
               .andExpect(content().string(String.valueOf(testPlayerId)));
    }

    @Test
    public void testGetGameOwnerByPersonId() throws Exception {
        assertNotNull(setupPerson);
        GameOwner gameOwner = gameOwnerRepo.findByPersonId(testUserId);
        assertNotNull(gameOwner, "GameOwner should exist for setup person from @BeforeEach");
        String expectedName = setupPerson.getName();

        mockMvc.perform(get("/game-owners")
                .param("person_id", String.valueOf(testUserId))
                .header("User-Id", String.valueOf(testUserId)))
               .andExpect(status().isOk())
               .andExpect(content().string(expectedName));
    }
}