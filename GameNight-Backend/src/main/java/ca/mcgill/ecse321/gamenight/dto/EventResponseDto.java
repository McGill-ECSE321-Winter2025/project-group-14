package ca.mcgill.ecse321.gamenight.dto;

import java.util.Date;

import ca.mcgill.ecse321.gamenight.model.Event;


public class EventResponseDto {
    
    private int id;
    private String name;
    private String description;
    private Date startTime;
    private Date endTime;

    public EventResponseDto() {
    }

   
    public EventResponseDto(Event event) {
        this.id = event.getId();
        this.name = event.getName();
        this.description = event.getDescription();
        this.startTime = event.getStartTime();
        this.endTime = event.getEndTime();
    }

    // Getters
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public Date getStartTime() {
        return startTime;
    }
    public Date getEndTime() {
        return endTime;
    }
}
