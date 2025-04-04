import React, { useState, useEffect, useCallback, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Box, Typography, Button, CircularProgress } from "@mui/material";
import { AuthContext } from "../../AuthContext";
import '../../styles/event-details.css';

function EventDetails() {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const { user } = useContext(AuthContext);

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
      }
    }
    fetchGames();
  }, [eventId]);

  useEffect(() => {
    async function checkRegistration() {
      if (!user || !user.userId) return;
      try {
        const playerResponse = await fetch(
          `http://localhost:8080/players?person_id=${user.userId}`
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
      alert("You must be logged in to register for an event.");
      return;
    }
    try {
      const playerResponse = await fetch(
        `http://localhost:8080/players?person_id=${user.userId}`
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
        alert("Registered successfully!");
        setIsRegistered(true);
      } else {
        alert("Error registering for event.");
      }
    } catch (error) {
      console.error("Error registering for event:", error);
    }
  };

  const handleUnregister = async () => {
    if (!user || !user.userId) {
      alert("You must be logged in to unregister for an event.");
      return;
    }
    try {
      const playerResponse = await fetch(
        `http://localhost:8080/players?person_id=${user.userId}`
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
        alert("Unregistered successfully!");
        setIsRegistered(false);
      } else {
        alert("Error unregistering for event.");
      }
    } catch (error) {
      console.error("Error unregistering for event:", error);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress className="custom-progress" />
      </Box>
    );
  }

  if (!event) {
    return (
      <Box className="event-details-container not-found-container">
        <Typography variant="h5" className="not-found-text">Event not found</Typography>
        <Button onClick={() => navigate("/events")} className="back-button">
          Back to Events
        </Button>
      </Box>
    );
  }

  // Check if event is expired
  const isExpired = event.endTime && new Date(event.endTime) < new Date();
 
  const formattedStart = new Date(event.startTime).toLocaleString("en-US", {
    month: "numeric",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "numeric",
    hour12: true,
  });
  const formattedEnd = new Date(event.endTime).toLocaleString("en-US", {
    month: "numeric",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "numeric",
    hour12: true,
  });

  return (
    <div className="event-details-container">
      <div className="event-details-card">
        {isExpired && <span className="expired-badge">Expired</span>}
       
        <Typography variant="h4" className="event-details-title">{event.name}</Typography>
       
        <div className="event-details-dates">
          <Typography variant="body2" className="event-date">
            <span className="date-icon">📅</span> Start: {formattedStart}
          </Typography>
          <Typography variant="body2" className="event-date">
            <span className="date-icon">⏱️</span> End: {formattedEnd}
          </Typography>
        </div>
       
        <Typography variant="body1" className="event-details-description">{event.description}</Typography>
       
        <div className="games-section">
          <Typography variant="subtitle1" className="games-section-title">
            <span className="games-icon">🎮</span> Games Scheduled:
          </Typography>
         
          {games && games.length > 0 ? (
            <ul className="games-list">
              {games.map((game) => (
                <li key={game.id} className="game-item">{game.name}</li>
              ))}
            </ul>
          ) : (
            <Typography variant="body2" className="no-games">
              No games scheduled for this event.
            </Typography>
          )}
        </div>
       
        {!isExpired && (
          <Box className="registration-actions">
            {isRegistered ? (
              <Button
                variant="contained"
                className="unregister-button"
                onClick={handleUnregister}
              >
                Unregister for this Event
              </Button>
            ) : (
              <Button
                variant="contained"
                className="register-button"
                onClick={handleRegister}
              >
                Register for this Event
              </Button>
            )}
          </Box>
        )}
       
        <Box className="navigation-actions">
          <Button
            variant="outlined"
            className="back-button"
            onClick={() => navigate("/events")}
          >
            Back to Events
          </Button>
        </Box>
      </div>
    </div>
  );
}

export default EventDetails;
