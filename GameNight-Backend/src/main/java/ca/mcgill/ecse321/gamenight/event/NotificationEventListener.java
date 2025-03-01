package ca.mcgill.ecse321.gamenight.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {
    @EventListener
    public void handleNotificationEvent(NotificationEvent event){
        int receiverId = event.getReceiverId();
        String message = event.getMessage();

    }
}
