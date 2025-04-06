import React, { useState, useEffect, useCallback, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { AuthContext } from "../../AuthContext";
import { usePopup } from "../../components/PopupContext"; 
import '../../styles/event-details.css';

function EventDetails() {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);
  const { showPopup } = usePopup(); 

  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isRegistered, setIsRegistered] = useState(false);
  const [games, setGames] = useState([]);

  const fetchEvent = useCallback(async () => {
    try {
      const response = await fetch(`http://localhost:8080/events/${eventId}`);
      if (!response.ok) {
        throw new Error("Failed to fetch event");
      }
      const data = await response.json();
      setEvent(data);
    } catch (error) {
      console.error("Error fetching event:", error);
      setEvent(null);
    } finally {
      setLoading(false);
    }
  }, [eventId]);

  useEffect(() => {
    fetchEvent();
  }, [fetchEvent]);

  useEffect(() => {
    async function fetchGames() {
      try {
        const response = await fetch(`http://localhost:8080/events/scheduledevent/${eventId}`);
        if (!response.ok) {
          console.error("Failed to fetch games for event");
          return;
        }
        const gamesData = await response.json();
        setGames(gamesData);
      } catch (error) {
        console.error("Error fetching games for event:", error);
        showPopup("Error fetching games for event: " + error.message);
      }
    }
    fetchGames();
  }, [eventId, showPopup]);

  useEffect(() => {
    async function checkRegistration() {
      if (!user || !user.userId) return;
      try {
        const playerResponse = await fetch(
          `http://localhost:8080/users/${user.userId}/player-id`
        );
        if (!playerResponse.ok) {
          console.error("Failed to fetch player id for registration check");
          return;
        }
        const playerId = await playerResponse.json();
        console.log("Fetched player id:", playerId);

        const playersResponse = await fetch(
          `http://localhost:8080/events/${eventId}/players`
        );
        if (!playersResponse.ok) {
          console.error("Failed to fetch players for event");
          return;
        }
        const players = await playersResponse.json();
        console.log("Fetched registered players:", players);

        const registered = players.some(
          (p) => Number(p.playerId) === Number(playerId)
        );
        setIsRegistered(registered);
      } catch (error) {
        console.error("Error checking registration:", error);
      }
    }
    checkRegistration();
  }, [eventId, user]);

  const handleRegister = async () => {
    if (!user || !user.userId) {
      showPopup("You must be logged in to register for an event.");
      return;
    }
    try {
      const playerResponse = await fetch(
        `http://localhost:8080/users/${user.userId}/player-id`
      );
      if (!playerResponse.ok) {
        throw new Error("Failed to fetch player id");
      }
      const playerId = await playerResponse.json();

      const registerResponse = await fetch(
        `http://localhost:8080/events/${eventId}/player/${playerId}`,
        { method: "POST" }
      );
      if (registerResponse.ok) {
        setIsRegistered(true);
      } else {
        showPopup("Error registering for event.");
      }
    } catch (error) {
      console.error("Error registering for event:", error);
      showPopup("Error registering for event: " + error.message);
    }
  };

  const handleUnregister = async () => {
    if (!user || !user.userId) {
      showPopup("You must be logged in to unregister for an event.");
      return;
    }
    try {
      const playerResponse = await fetch(
        `http://localhost:8080/users/${user.userId}/player-id`
      );
      if (!playerResponse.ok) {
        throw new Error("Failed to fetch player id");
      }
      const playerId = await playerResponse.json();

      const unregisterResponse = await fetch(
        `http://localhost:8080/events/${eventId}/player/${playerId}`,
        { method: "DELETE" }
      );
      if (unregisterResponse.ok) {
        setIsRegistered(false);
      } else {
        showPopup("Error unregistering for event.");
      }
    } catch (error) {
      console.error("Error unregistering for event:", error);
      showPopup("Error unregistering for event: " + error.message);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="custom-progress"></div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="event-details-container not-found-container">
        <h5 className="not-found-text">Event not found</h5>
        <button onClick={() => navigate("/events")} className="back-button">
          Back to Events
        </button>
      </div>
    );
  }

  const isExpired = event.endTime && new Date(event.endTime) < new Date();
 
  const hasValidStartTime = event.startTime && new Date(event.startTime).getTime() > 0;
  const hasValidEndTime = event.endTime && new Date(event.endTime).getTime() > 0;

  const formattedStart = hasValidStartTime
    ? new Date(event.startTime).toLocaleString("en-US", {
        month: "numeric",
        day: "numeric",
        year: "numeric",
        hour: "numeric",
        minute: "numeric",
        hour12: true,
      })
    : null;

  const formattedEnd = hasValidEndTime
    ? new Date(event.endTime).toLocaleString("en-US", {
        month: "numeric",
        day: "numeric",
        year: "numeric",
        hour: "numeric",
        minute: "numeric",
        hour12: true,
      })
    : null;

  return (
    <div className="event-details-container">
      <div className="event-details-card">
        {isExpired && <span className="expired-badge">Expired</span>}
     
        <h4 className="event-details-title">{event.name}</h4>
     
        <div className="event-details-dates">
          {hasValidStartTime && (
            <p className="event-date">
               Start: {formattedStart}
            </p>
          )}
          {hasValidEndTime && (
            <p className="event-date">
               End: {formattedEnd}
            </p>
          )}
        </div>
     
        <p className="event-details-description">{event.description}</p>
     
        <div className="games-section">
          <h5 className="games-section-title">
             Games Scheduled:
          </h5>
       
          {games && games.length > 0 ? (
            <ul className="games-list">
              {games.map((game) => (
                <li key={game.id} className="game-item">{game.name}</li>
              ))}
            </ul>
          ) : (
            <p className="no-games">
              No games scheduled for this event.
            </p>
          )}
        </div>
     
        {!isExpired && (
          <div className="registration-actions">
            {isRegistered ? (
              <button
                className="unregister-button"
                onClick={handleUnregister}
              >
                Unregister for this Event
              </button>
            ) : (
              <button
                className="register-button"
                onClick={handleRegister}
              >
                Register for this Event
              </button>
            )}
          </div>
        )}
     
        <div className="navigation-actions">
          <button
            className="back-button"
            onClick={() => navigate("/events")}
          >
            Back to Events
          </button>
        </div>
      </div>
    </div>
  );
}

export default EventDetails;