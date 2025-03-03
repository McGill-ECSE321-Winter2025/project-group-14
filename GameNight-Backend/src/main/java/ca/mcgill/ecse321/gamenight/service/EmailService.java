package ca.mcgill.ecse321.gamenight.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.model.Person;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender eMailSender;

    @Value("${spring.mail.username:no-reply@gamenight.com}")
    private String fromEmail;

    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            eMailSender.send(message);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to send email");        
        }
    }
    @Async
    public void sendBorrowingRequestEmail(String to, Person playerName, String gameName) {
        String subject = "New Borrowing Request";
        String text = "Hello,\n\n" +
                     "You have received a new borrowing request from " + playerName.getName() + 
                     " for your game \"" + gameName + "\".\n\n" +
                     "Please log in to respond to this request.\n\n" +
                     "Best regards,\nGame Night Team";
        
        sendSimpleEmail(to, subject, text);
    } 

    @Async
    public void sendRequestAcceptedEmail(String to, String ownerName, String gameName) {
        String subject = "Borrowing Request Accepted";
        String text = "Hello,\n\n" +
                     "Good news! " + ownerName + " has accepted your request to borrow " +
                     "the game \"" + gameName + "\".\n\n" +
                     "Please log in to view the details.\n\n" +
                     "Best regards,\nGame Night Team";
        
        sendSimpleEmail(to, subject, text);
    }

    @Async
    public void sendRequestRejectedEmail(String to, String ownerName, String gameName) {
        String subject = "Borrowing Request Rejected";
        String text = "Hello,\n\n" +
                     "We regret to inform you that " + ownerName + " has declined your request to borrow " +
                    "the game \"" + gameName + "\".\n\n" +
                    "You may try borrowing another game or contact the owner for more details.\n\n" +
                    "Best regards,\nGame Night Team";
        
        sendSimpleEmail(to, subject, text);
    }


}
