package ca.mcgill.ecse321.gamenight.event;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;

@Component
public class NotificationEventListener {

    @Autowired
    private PersonRepository personRepository;
   
    @EventListener
    public void handleNotificationEvent(NotificationEvent event){
        int receiverId = event.getReceiverId();
        String message = event.getMessage();

        Optional<Person> personOpt = personRepository.findById(receiverId);
     
    }
   
    }
