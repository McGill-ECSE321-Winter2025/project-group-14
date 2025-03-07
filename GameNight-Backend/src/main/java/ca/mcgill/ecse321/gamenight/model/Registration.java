package ca.mcgill.ecse321.gamenight.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class Registration {

	@EmbeddedId
	private Key key;

	public Registration() {
	}

	public Registration(Key key) {
		this.key = key;
	}

	public Key getKey() {
		return key;
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