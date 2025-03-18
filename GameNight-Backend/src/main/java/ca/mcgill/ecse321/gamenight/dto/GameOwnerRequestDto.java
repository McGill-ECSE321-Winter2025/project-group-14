package ca.mcgill.ecse321.gamenight.dto;

public class GameOwnerRequestDto {
    private int personId;
    private boolean isActive;

    public GameOwnerRequestDto() {
    }

    public int getPersonId() {
        return personId;
    }

    public boolean getIsActive() {
        return isActive;
    }
}