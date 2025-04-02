package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ca.mcgill.ecse321.gamenight.controller.UserManagementController;
import ca.mcgill.ecse321.gamenight.dto.AuthRequestDto;
import ca.mcgill.ecse321.gamenight.exception.InvalidInputException;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.exception.UniquenessConstaintException;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;

public class UserManagementServiceTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameOwnerRepository gameOwnerRepository;

    @Mock
    private UserManagementService userService;

    @Mock
    private UserManagementController userController;

    @InjectMocks
    private UserManagementService userManagementService;

    private static final String VALID_EMAIL = "testuser@gmail.com";
    private static final String INVALID_EMAIL = "invalid-email";
    private static final String EMPTY_STRING = "";
    private static final String VALID_PASSWORD = "password123";
    private static final String NEW_EMAIL = "newemail@gmail.com";

    private Person testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new Person(VALID_EMAIL, VALID_PASSWORD, "Test User");
        when(personRepository.findPersonByEmailAddress(VALID_EMAIL))
                .thenReturn(Optional.of(testUser));
        when(personRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));
        when(personRepository.save(any(Person.class))).thenReturn(testUser);
    }

    /**
     * Tests validation of a valid email and non-empty password (should pass).
     */
    @Test
    public void testValidateEmailAndPasswordSuccess() {
        AuthRequestDto request = new AuthRequestDto("1" + VALID_EMAIL, VALID_PASSWORD, "User");
        assertDoesNotThrow(() -> userManagementService.createPerson(request));
    }

    /**
     * Tests validation failure when email is empty.
     */
    @Test
    public void testValidateEmptyEmail() {
        AuthRequestDto request = new AuthRequestDto(EMPTY_STRING, VALID_PASSWORD, "User");

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> userManagementService.createPerson(request));

        assertEquals("Email adress cannot be empty", exception.getMessage());
    }

    /**
     * Tests validation failure when email format is invalid.
     */
    @Test
    public void testValidateBadFormatEmail() {
        AuthRequestDto request = new AuthRequestDto(INVALID_EMAIL, VALID_PASSWORD, "User");

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> userManagementService.createPerson(request));

        assertEquals("Invalid email pattern", exception.getMessage());
    }

    /**
     * Tests validation failure when email or password has only whitespace.
     */
    @Test
    public void testValidateWhitespaceEmailAndPassword() {
        assertThrows(InvalidInputException.class,
                () -> userManagementService.createPerson(new AuthRequestDto("  ", VALID_PASSWORD, "User")));
        assertThrows(InvalidInputException.class,
                () -> userManagementService.createPerson(new AuthRequestDto(VALID_EMAIL, "  ", "User")));

    }

    /**
     * Tests validation failure when password is empty.
     */
    @Test
    public void testValidateEmptyPassword() {
        AuthRequestDto request = new AuthRequestDto("1" + VALID_EMAIL, EMPTY_STRING, "User");

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> userManagementService.createPerson(request));

        assertEquals("Password cannot be empty", exception.getMessage());
    }

    /**
     * Tests successful user creation
     */
    @Test
    public void testCreatePersonSuccess() {
        when(playerRepository.save(any(Player.class))).thenReturn(new Player(testUser));
        when(gameOwnerRepository.save(any(GameOwner.class))).thenReturn(new GameOwner(testUser));

        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress("newuser@gmail.com");
        request.setPassword("securePassword");
        request.setName("New User");

        // This ensures the user doesn't exist, so creation should succeed
        when(personRepository.findPersonByEmailAddress(request.getEmailAdress()))
                .thenReturn(Optional.empty());

        userManagementService.createPerson(request);

        verify(personRepository, times(1)).save(any(Person.class));
        verify(playerRepository, times(1)).save(any(Player.class));
        verify(gameOwnerRepository, times(1)).save(any(GameOwner.class));
    }

    /**
     * Tests user creation failure due to duplicate email
     */
    @Test
    public void testCreatePersonDuplicateEmail() {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);
        request.setName("Duplicate User");

        // Because testUser is found for this email, we expect an exception
        assertThrows(UniquenessConstaintException.class, () -> userManagementService.createPerson(request));
    }

    /**
     * Tests successful login
     */
    @Test
    public void testLoginSuccess() {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);

        Person loggedInUser = userManagementService.login(request);

        assertNotNull(loggedInUser);
        assertEquals(testUser.getId(), loggedInUser.getId());
    }

    /**
     * Tests login failure due to empty password
     */
    @Test
    public void testLoginEmptyPassword() {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress(VALID_EMAIL);
        request.setPassword(EMPTY_STRING);

        assertThrows(InvalidInputException.class, () -> userManagementService.login(request));
    }

    /**
     * Tests login failure due to empty email
     */
    @Test
    public void testLoginEmptyEmail() {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress(EMPTY_STRING);
        request.setPassword(VALID_PASSWORD);

        assertThrows(InvalidInputException.class, () -> userManagementService.login(request));
    }

    /**
     * Tests login failure due to wrong password
     */
    @Test
    public void testLoginWrongPassword() {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress(VALID_EMAIL);
        request.setPassword("defNotCorrect");

        assertThrows(InvalidInputException.class, () -> userManagementService.login(request));
    }

    /**
     * Tests login failure due to non-existent user
     */
    @Test
    public void testLoginUserNotFound() {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmailAdress("nonexistent@gmail.com");
        request.setPassword(VALID_PASSWORD);

        when(personRepository.findPersonByEmailAddress(request.getEmailAdress()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidInputException.class, () -> userManagementService.login(request));
    }

    /**
     * Tests successful user update
     */
    @Test
    public void testUpdateUserSuccess() {
        // The user is found => we can update
        when(personRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        boolean result = userManagementService.updatePerson(testUser.getId(), VALID_PASSWORD, NEW_EMAIL, null);

        assertTrue(result);
        assertEquals(NEW_EMAIL, testUser.getEmailAddress());
        verify(personRepository, times(1)).save(testUser);
    }

    /**
     * Tests user update failure due to incorrect old password
     */
    @Test
    public void testUpdateUserIncorrectPassword() {
        boolean result = userManagementService.updatePerson(testUser.getId(), EMPTY_STRING, NEW_EMAIL, null);

        assertFalse(result);
        assertNotEquals(NEW_EMAIL, testUser.getEmailAddress());
        verify(personRepository, never()).save(any(Person.class));
    }

    /**
     * Tests that updating a user with the same email and password does not trigger
     * a save operation.
     */
    @Test
    public void testUpdateUserWithSameEmailAndPassword() {
        boolean result = userManagementService.updatePerson(
                testUser.getId(), VALID_PASSWORD, VALID_EMAIL, VALID_PASSWORD);

        assertTrue(result);
        // Email & password remain the same
        assertEquals(VALID_EMAIL, testUser.getEmailAddress());
        assertEquals(VALID_PASSWORD, testUser.getPassword());
        verify(personRepository, never()).save(any(Person.class));
    }

    /**
     * Tests that updating a user without providing new email or password does not
     * trigger a save operation.
     */
    @Test
    public void testUpdateUserNoChanges() {
        boolean result = userManagementService.updatePerson(testUser.getId(), VALID_PASSWORD, null, null);

        assertTrue(result);
        assertEquals(VALID_EMAIL, testUser.getEmailAddress());
        assertEquals(VALID_PASSWORD, testUser.getPassword());
        verify(personRepository, never()).save(any(Person.class));
    }

    /**
     * Tests updating the user's new password while keeping same email
     */
    @Test
    public void testUpdateUserWithSameEmailNewPassword() {
        boolean result = userManagementService.updatePerson(
                testUser.getId(), VALID_PASSWORD, VALID_EMAIL, "newSecurePassword");

        assertTrue(result);
        assertEquals(VALID_EMAIL, testUser.getEmailAddress());
        assertEquals("newSecurePassword", testUser.getPassword());
        verify(personRepository, times(1)).save(testUser);
    }

    /**
     * Tests updating a user's email and keeping same password
     */
    @Test
    public void testUpdateUserWithSamePasswordNewEmail() {
        boolean result = userManagementService.updatePerson(
                testUser.getId(), VALID_PASSWORD, NEW_EMAIL, VALID_PASSWORD);

        assertTrue(result);
        assertEquals(NEW_EMAIL, testUser.getEmailAddress());
        assertEquals(VALID_PASSWORD, testUser.getPassword());
        verify(personRepository, times(1)).save(testUser);
    }

    /**
     * Tests updating a user's email and password when the password is blank " "
     */
    @Test
    public void testUpdateUserWithBlankPassword() {
        boolean result = userManagementService.updatePerson(testUser.getId(), VALID_PASSWORD, NEW_EMAIL, "  ");

        assertTrue(result);
        assertEquals(NEW_EMAIL, testUser.getEmailAddress());
        assertEquals(VALID_PASSWORD, testUser.getPassword()); // Password should remain unchanged
        verify(personRepository, times(1)).save(testUser);
    }

    /**
     * Tests successful user deletion
     */
    @Test
    public void testDeletePersonSuccess() {
        GameOwner testGameOwner = new GameOwner(testUser);
        Player testPlayer = new Player(testUser);

        when(gameOwnerRepository.findByPersonId(testUser.getId())).thenReturn(testGameOwner);
        when(playerRepository.findByPersonId(testUser.getId())).thenReturn(testPlayer);
        when(personRepository.findPersonById(testUser.getId())).thenReturn(Optional.of(testUser));

        userManagementService.deletePerson(testUser.getId());

        verify(gameOwnerRepository, times(1)).delete(any(GameOwner.class));
        verify(playerRepository, times(1)).delete(any(Player.class));
        verify(personRepository, times(1)).delete(testUser);
    }

    /**
     * Tests user deletion failure for non-existent user
     */
    @Test
    public void testDeletePersonNotFound() {
        when(personRepository.findPersonById(testUser.getId())).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> userManagementService.deletePerson(999));

        verify(personRepository, never()).delete(any(Person.class));
    }

    /**
     * Tests deleting a non-existing user
     */
    @Test
    public void testDeleteAnotherUser() {
        when(personRepository.findPersonById(999)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> userManagementService.deletePerson(999));
    }

    /**
     * Tests toggling to a GameOwner from Player
     */
    @Test
    public void testToggleRoleToGameOwner() {
        Person person = new Person(VALID_EMAIL, VALID_PASSWORD, "Test User");
        GameOwner gameOwnerRole = new GameOwner(person);
        gameOwnerRole.setActive(false);

        // Correct mock to match the service method
        when(gameOwnerRepository.findByPersonId(person.getId())).thenReturn(gameOwnerRole);
        when(gameOwnerRepository.save(any(GameOwner.class))).thenReturn(gameOwnerRole);

        userManagementService.toggleAccountRole(person.getId());

        assertTrue(gameOwnerRole.isActive());
        verify(gameOwnerRepository, times(1)).save(gameOwnerRole);
    }

    /**
     * Tests toggling to a player from gameOwner
     */
    @Test
    public void testToggleRoleToPlayer() {
        Person person = new Person(VALID_EMAIL, VALID_PASSWORD, "Test User");
        GameOwner gameOwnerRole = new GameOwner(person);
        gameOwnerRole.setActive(true);

        when(gameOwnerRepository.findByPersonId(person.getId())).thenReturn(gameOwnerRole);
        when(gameOwnerRepository.save(any(GameOwner.class))).thenReturn(gameOwnerRole);

        userManagementService.toggleAccountRole(person.getId());

        assertFalse(gameOwnerRole.isActive());
        verify(gameOwnerRepository, times(1)).save(gameOwnerRole);
    }

    /**
     * Tests getting all existing users
     */
    @Test
    public void testGettingAllUsers() {
        Person hamza = new Person("hamza@gmail.com", "Helloworld2223", "Hamza");
        Person deniz = new Person("deniz@gmail.com", "Helloworld", "Deniz");
        List<Person> testUsers = Arrays.asList(hamza, deniz);

        when(personRepository.findAll()).thenReturn(testUsers);

        List<Person> allUsers = userManagementService.getAllUsers();
        assertNotNull(allUsers);
        assertEquals(2, allUsers.size());
        assertEquals("Hamza", allUsers.get(0).getName());
        assertEquals("Deniz", allUsers.get(1).getName());
        verify(personRepository, times(1)).findAll();
    }

    /**
     * Tests getting all users but none exist yet
     */
    @Test
    public void testGettingAllUsersWhenThereIsNoUsers() {
        when(personRepository.findAll()).thenReturn(List.of());
        List<Person> allUsers = userManagementService.getAllUsers();

        assertNotNull(allUsers, "The returned list should not be null");
        assertTrue(allUsers.isEmpty(), "The returned list should be empty.");
        verify(personRepository, times(1)).findAll();
    }

    /**
     * Tests getting an existing user by ID
     */
    @Test
    public void testGetExistingUserById() {
        Person hamza = new Person("testuser@gmail.com", "password123", "Test User");

        when(personRepository.findById(hamza.getId())).thenReturn(Optional.of(hamza));

        Person foundUser = userManagementService.getUserById(hamza.getId());

        assertNotNull(foundUser, "The returned user should not be null.");
        assertEquals(hamza.getId(), foundUser.getId(), "The IDs should match.");
        assertEquals(hamza.getEmailAddress(), foundUser.getEmailAddress(), "The emails should match.");
        assertEquals(hamza.getPassword(), foundUser.getPassword(), "The passwords should match.");
        assertEquals(hamza.getName(), foundUser.getName(), "The names should match.");

        verify(personRepository, times(1)).findById(hamza.getId());
    }

    /**
     * Tests getting user from an invalid ID
     */
    @Test
    public void testGetNonExistingUserById() {
        int nonExistingId = 3;

        when(personRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userManagementService.getUserById(nonExistingId);
        }, "Expected RuntimeException when user is not found");
        verify(personRepository, times(1)).findById(nonExistingId);
    }

    /**
     * Tests that toggling an account role for a non-existent user throws a
     * ResponseStatusException.
     */
    @Test
    void testToggleAccountRole_userNotFound_throwsResponseStatusException() {
        int nonExistentId = 123;

        when(gameOwnerRepository.findByPersonId(nonExistentId)).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> {
            userManagementService.toggleAccountRole(nonExistentId);
        });
    }

    /**
     * Tests that attempting to update a non-existent user throws a
     * RuntimeException.
     */
    @Test
    void testUpdatePerson_userNotFound_throwsRuntimeException() {
        int nonExistentId = 999;
        when(personRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            userManagementService.updatePerson(nonExistentId, "someOldPassword", null, null);
        });
    }
}
