import React, { useState, useEffect, useCallback, useContext } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Box, Typography, Button, CircularProgress } from "@mui/material";
import { AuthContext } from "../AuthContext"; // If you have a user context

function EventDetails() {
  const { eventId } = useParams();
  const navigate = useNavigate();
  
  const { user } = useContext(AuthContext); 

  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);

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

  const handleRegister = async () => {
    if (!user || !user.userId) {
      alert("You must be logged in to register for an event.");
      return;
    }

    try {
      const response = await fetch(
        `http://localhost:8080/events/${eventId}/player/${user.userId}`,
        { method: "POST" }
      );
      if (response.ok) {
        alert("Registered successfully!");
      } else {
        alert("Error registering for event.");
      }
    } catch (error) {
      console.error("Error registering for event:", error);
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

  return (
    <Box className="container">
      <Typography variant="h4">{event.name}</Typography>
      <Typography variant="body1">{event.description}</Typography>
      <Typography variant="body2">
        Start: {new Date(event.startTime).toLocaleString()}
      </Typography>
      <Typography variant="body2">
        End: {new Date(event.endTime).toLocaleString()}
      </Typography>
      
      <Box mt={2}>
        <Button variant="contained" color="primary" onClick={handleRegister}>
          Register for this Event
        </Button>
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
