package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gamenight.dto.AuthRequest;
import ca.mcgill.ecse321.gamenight.exceptions.InvalidCredentialsException;
import ca.mcgill.ecse321.gamenight.exceptions.UsernameTakenException;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;

@SpringBootTest
public class UserManagementServiceTest {

    @Mock
    private PersonRepository personRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private GameOwnerRepository gameOwnerRepository;

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
        testUser = new Person(VALID_EMAIL, VALID_PASSWORD, "Test User");
        when(personRepository.findPersonByEmailAddress(VALID_EMAIL)).thenReturn(Optional.of(testUser));
        when(personRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(personRepository.save(any(Person.class))).thenReturn(testUser);
    }

    /**
     * Tests validation of a valid email and non-empty password (should pass).
     */
    @Test
    public void testValidateEmailAndPasswordSuccess() {
        AuthRequest request = new AuthRequest("1" + VALID_EMAIL, VALID_PASSWORD, "User");
        assertDoesNotThrow(() -> userManagementService.createPerson(request));
    }

    /**
     * Tests validation failure when email is empty.
     */
    @Test
    public void testValidateEmptyEmail() {
        AuthRequest request = new AuthRequest(EMPTY_STRING, VALID_PASSWORD, "User");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userManagementService.createPerson(request));

        assertEquals("Email adress cannot be empty", exception.getMessage());
    }

    /**
     * Tests validation failure when email is empty.
     */
    @Test
    public void testValidateBadFormatEmail() {
        AuthRequest request = new AuthRequest(INVALID_EMAIL, VALID_PASSWORD, "User");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userManagementService.createPerson(request));

        assertEquals("Invalid email pattern", exception.getMessage());
    }

    /**
     * Tests validation failure when email format is invalid.
     */
    @Test
    public void testValidateWhitespaceEmailAndPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> userManagementService.createPerson(new AuthRequest("  ", VALID_PASSWORD, "User")));
        assertThrows(IllegalArgumentException.class,
                () -> userManagementService.createPerson(new AuthRequest(VALID_EMAIL, "  ", "User")));
    }

    /**
     * Tests validation failure when password is empty.
     */
    @Test
    public void testValidateEmptyPassword() {
        AuthRequest request = new AuthRequest("1" + VALID_EMAIL, EMPTY_STRING, "User");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
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
        AuthRequest request = new AuthRequest();
        request.setEmailAdress("newuser@gmail.com");
        request.setPassword("securePassword");
        request.setName("New User");

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
        AuthRequest request = new AuthRequest();
        request.setEmailAdress(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);
        request.setName("Duplicate User");

        assertThrows(UsernameTakenException.class, () -> userManagementService.createPerson(request));
    }

    /**
     * Tests successful login
     */
    @Test
    public void testLoginSuccess() {
        AuthRequest request = new AuthRequest();
        request.setEmailAdress(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);

        Person loggedInUser = userManagementService.login(request);

        assertNotNull(loggedInUser);
        assertEquals(testUser.getId(), loggedInUser.getId());
    }

    /**
     * Tests login failure due to incorrect password
     */
    @Test
    public void testLoginEmptyPassword() {
        AuthRequest request = new AuthRequest();
        request.setEmailAdress(VALID_EMAIL);
        request.setPassword(EMPTY_STRING);

        assertThrows(java.lang.IllegalArgumentException.class, () -> userManagementService.login(request));
    }

    @Test
    public void testLoginEmptyEmail() {
        AuthRequest request = new AuthRequest();
        request.setEmailAdress(EMPTY_STRING);
        request.setPassword(VALID_PASSWORD);

        assertThrows(java.lang.IllegalArgumentException.class, () -> userManagementService.login(request));
    }

    @Test
    public void testLoginWrongPassword() {
        AuthRequest request = new AuthRequest();
        request.setEmailAdress(VALID_EMAIL);
        request.setPassword("defNotCorrect");

        assertThrows(InvalidCredentialsException.class, () -> userManagementService.login(request));
    }

    /**
     * Tests login failure due to non-existent user
     */
    @Test
    public void testLoginUserNotFound() {
        AuthRequest request = new AuthRequest();
        request.setEmailAdress("nonexistent@gmail.com");
        request.setPassword(VALID_PASSWORD);

        when(personRepository.findPersonByEmailAddress(request.getEmailAdress()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userManagementService.login(request));
    }

    /**
     * Tests successful user update
     */
    @Test
    public void testUpdateUserSuccess() {
        when(personRepository.findById(testUser.getId()))
                .thenReturn(Optional.of(testUser));

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
        verify(personRepository, times(0)).save(any(Person.class));
    }

    @Test
    public void testUpdateUserWithSameEmailAndPassword() {
        boolean result = userManagementService.updatePerson(testUser.getId(), VALID_PASSWORD, VALID_EMAIL,
                VALID_PASSWORD);

        assertTrue(result);
        assertEquals(VALID_EMAIL, testUser.getEmailAddress());
        assertEquals(VALID_PASSWORD, testUser.getPassword());
        verify(personRepository, times(0)).save(any(Person.class));
    }

    @Test
    public void testUpdateUserNoChanges() {
        boolean result = userManagementService.updatePerson(testUser.getId(), VALID_PASSWORD, null, null);

        assertTrue(result);
        assertEquals(VALID_EMAIL, testUser.getEmailAddress());
        assertEquals(VALID_PASSWORD, testUser.getPassword());
        verify(personRepository, times(0)).save(any(Person.class));
    }

    @Test
    public void testUpdateUserWithSameEmailNewPassword() {
        boolean result = userManagementService.updatePerson(testUser.getId(), VALID_PASSWORD, VALID_EMAIL,
                "newSecurePassword");

        assertTrue(result);
        assertEquals(VALID_EMAIL, testUser.getEmailAddress());
        assertEquals("newSecurePassword", testUser.getPassword());
        verify(personRepository, times(1)).save(testUser);
    }

    @Test
    public void testUpdateUserWithSamePasswordNewEmail() {
        boolean result = userManagementService.updatePerson(testUser.getId(), VALID_PASSWORD, NEW_EMAIL,
                VALID_PASSWORD);

        assertTrue(result);
        assertEquals(NEW_EMAIL, testUser.getEmailAddress());
        assertEquals(VALID_PASSWORD, testUser.getPassword());
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
        when(personRepository.findPersonById(testUser.getId()))
                .thenReturn(Optional.of(testUser));

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
        when(personRepository.findPersonById(testUser.getId()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userManagementService.deletePerson(testUser.getId()));

        verify(personRepository, times(0)).delete(any(Person.class));
    }

    @Test
    public void testDeleteAnotherUser() {
        when(personRepository.findPersonById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userManagementService.deletePerson(999));
    }

}
