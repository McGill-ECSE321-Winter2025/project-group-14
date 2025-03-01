package ca.mcgill.ecse321.gamenight.event;

import org.springframework.context.ApplicationEvent;

public class NotificationEvent extends ApplicationEvent{
    private String message;
    private int receiverId;

    public NotificationEvent (Object source, int receiverId, String message){
        super(source);
        this.receiverId = receiverId;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getReceiverId() {
        return receiverId;
    }
    
}
