package ca.mcgill.ecse321.gamenight.dto;

import ca.mcgill.ecse321.gamenight.model.GameOwner;

public class GameOwnerResponseDto {
    private int ownerId;
    private PersonResponseDto person;
    private boolean isActive;

    public GameOwnerResponseDto(GameOwner owner) {
        this.ownerId = owner.getId();
        this.person = new PersonResponseDto(owner.getPerson());
        this.isActive = owner.isActive();
    }

    public int getOwnerId() {
        return ownerId;
    }

    public PersonResponseDto getPerson() {
        return person;
    }

    public boolean getIsActive() {
        return isActive;
    }
}
