package ca.mcgill.ecse321.gamenight.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class Registration {

    @EmbeddedId
    private Key key;
    
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Registration() {
        this.createdAt = new Date();
    }

    public Registration(Key key) {
        this.key = key;
        this.createdAt = new Date();
    }

    public Key getKey() {
        return key;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }

    @Embeddable
    public static class Key implements Serializable {
        @ManyToOne
        private Player player;
        @ManyToOne
        private Event event;

        public Key() {
        }

        public Key(Player player, Event event) {
            this.player = player;
            this.event = event;
        }

        public Player getPlayer() {
            return player;
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
            return this.player.getId() == otherKey.player.getId()
                    && this.event.getId() == otherKey.event.getId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.player.getId(), this.event.getId());
        }
    }
}
