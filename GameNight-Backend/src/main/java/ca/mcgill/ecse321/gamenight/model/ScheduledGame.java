package ca.mcgill.ecse321.gamenight.model;

public class ScheduledGame {

    private int id;
    private Game game;
    private Event event;

    public ScheduledGame() {
    }

    public ScheduledGame(Game game, Event event) {
        this.game = game;
        this.event = event;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
