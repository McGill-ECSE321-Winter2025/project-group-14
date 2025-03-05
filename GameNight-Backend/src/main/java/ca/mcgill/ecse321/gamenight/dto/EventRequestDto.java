package ca.mcgill.ecse321.gamenight.dto;

import java.util.Date;

/**
 * DTO (Data Transfer Object) for incoming event data.
 * This resembles your "BorrowingRequestRequestDto" pattern:
 * used when creating or updating an event via a POST/PUT request.
 */
public class EventRequestDto {

    private String name;
    private String description;
    private Date startTime;
    private Date endTime;

    // No-arg constructor needed for deserialization
    public EventRequestDto() {
    }

    public EventRequestDto(String name, String description, Date startTime, Date endTime) {
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters
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

    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}

