package ca.mcgill.ecse321.gamenight.dto;

import ca.mcgill.ecse321.gamenight.model.Player;

public class PlayerResponseDto {
    private int playerId;
    private PersonResponseDto person;

    public PlayerResponseDto(Player player) {
        this.playerId = player.getId();
        this.person = new PersonResponseDto(player.getPerson());
    }

    public int getPlayerId() {
        return playerId;
    }

    public PersonResponseDto getPerson() {
        return person;
    }
}
