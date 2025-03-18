package ca.mcgill.ecse321.gamenight.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ca.mcgill.ecse321.gamenight.exceptions.EmailSendingFailedException;
import ca.mcgill.ecse321.gamenight.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "rehz2004@gmail.com");
    }

    @SuppressWarnings("null")
    @Test
    public void testSendSimpleEmailSuccess() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test email text";

        emailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertEquals("rehz2004@gmail.com", capturedMessage.getFrom());
        assertEquals(to, capturedMessage.getTo()[0]);
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(text, capturedMessage.getText());
    }

    @Test
    public void testSendSimpleEmailFailure() {
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test email text";

        assertThrows(EmailSendingFailedException.class, () -> {
            emailService.sendSimpleEmail(to, subject, text);
        });
    }

    @SuppressWarnings("null")
    @Test
    public void testSendBorrowingRequestEmail() {
        Person player = mock(Person.class);
        when(player.getName()).thenReturn("John Doe");

        String to = "recipient@example.com";
        String gameName = "Chess";

        emailService.sendBorrowingRequestEmail(to, player, gameName);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertEquals("rehz2004@gmail.com", capturedMessage.getFrom());
        assertEquals(to, capturedMessage.getTo()[0]);
        assertEquals("New Borrowing Request", capturedMessage.getSubject());
        assertTrue(capturedMessage.getText().contains("John Doe"));
        assertTrue(capturedMessage.getText().contains(gameName));
    }

    @SuppressWarnings("null")
    @Test
    public void testSendRequestAcceptedEmail() {
        String to = "recipient@example.com";
        String ownerName = "Alice";
        String gameName = "Monopoly";

        emailService.sendRequestAcceptedEmail(to, ownerName, gameName);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertEquals("rehz2004@gmail.com", capturedMessage.getFrom());
        assertEquals(to, capturedMessage.getTo()[0]);
        assertEquals("Borrowing Request Accepted", capturedMessage.getSubject());
        assertTrue(capturedMessage.getText().contains(ownerName));
        assertTrue(capturedMessage.getText().contains(gameName));
    }

    @SuppressWarnings("null")
    @Test
    public void testSendRequestRejectedEmail() {
        String to = "recipient@example.com";
        String ownerName = "Bob";
        String gameName = "Scrabble";

        emailService.sendRequestRejectedEmail(to, ownerName, gameName);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage capturedMessage = messageCaptor.getValue();

        assertEquals("rehz2004@gmail.com", capturedMessage.getFrom());
        assertEquals(to, capturedMessage.getTo()[0]);
        assertEquals("Borrowing Request Rejected", capturedMessage.getSubject());
        assertTrue(capturedMessage.getText().contains(ownerName));
        assertTrue(capturedMessage.getText().contains(gameName));
    }
}
