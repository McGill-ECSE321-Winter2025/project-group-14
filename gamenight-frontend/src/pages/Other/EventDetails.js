
import React, { useState, useEffect, useCallback, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Box, Typography, Button, CircularProgress } from "@mui/material";
import { AuthContext } from "../../AuthContext"; 

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
        <CircularProgress />
      </Box>
    );
  }

  if (!event) {
    return (
      <Box>
        <Typography variant="h5">Event not found</Typography>
        <Button onClick={() => navigate("/events")}>Back to Events</Button>
      </Box>
    );
  }

  
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
    <Box className="container">
      <Typography variant="h4">{event.name}</Typography>
      
      {}
      {games && games.length > 0 ? (
        <Box mb={2}>
          <Typography variant="subtitle1">Games Scheduled:</Typography>
          <ul>
            {games.map((game) => (
              <li key={game.id}>{game.name}</li>
            ))}
          </ul>
        </Box>
      ) : (
        <Box mb={2}>
          <Typography variant="subtitle1">No games scheduled.</Typography>
        </Box>
      )}
      
      <Typography variant="body1">{event.description}</Typography>
      <Typography variant="body2">Start: {formattedStart}</Typography>
      <Typography variant="body2">End: {formattedEnd}</Typography>
      
      <Box mt={2}>
        {isRegistered ? (
          <Button variant="contained" color="secondary" onClick={handleUnregister}>
            Unregister for this Event
          </Button>
        ) : (
          <Button variant="contained" color="primary" onClick={handleRegister}>
            Register for this Event
          </Button>
        )}
      </Box>
      <Box mt={2}>
        <Button variant="outlined" onClick={() => navigate("/events")}>
          Back to Events
        </Button>
      </Box>
    </Box>
  );
}

export default EventDetails;
