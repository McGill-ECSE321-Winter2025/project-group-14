package ca.mcgill.ecse321.gamenight.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class ScheduledGame {

    @EmbeddedId
    private Key key;

    public ScheduledGame() {
    }

    public ScheduledGame(Key key) {
        this.key = key;
    }

    public Key getKey() {
        return key;
    }

    @Embeddable
    public static class Key implements Serializable {
        @ManyToOne
        private Game game;
        @ManyToOne
        private Event event;

        public Key() {
        }

        public Key(Game game, Event event) {
            this.game = game;
            this.event = event;
        }

        public Game getGame() {
            return game;
        }

        public Event getEvent() {
            return event;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key otherKey = (Key) obj;
            return this.game.getId() == otherKey.game.getId()
                    && this.event.getId() == otherKey.event.getId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(game.getId(), event.getId());
        }
    }
}