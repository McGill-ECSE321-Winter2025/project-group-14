
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../../AuthContext';
import '../../styles/layout.css';
import '../../styles/event-history.css';
import { Box, CircularProgress,Button  } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import '../../styles/button.css';


const EventCard = ({ event, playerId, isCreator = false }) => {
    const [scheduledGames, setScheduledGames] = useState([]);
    const [isLoadingGames, setIsLoadingGames] = useState(false);
    const [errorGames, setErrorGames] = useState(null);

   
    const formatDateAndTime = (dateString) => {
        const d = new Date(dateString);
        const datePart = d.toLocaleDateString();
        const timePart = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        return `${datePart} ${timePart}`;
    };

   
    let dateLine = "";
    if (event.startTime || event.endTime) {
       
        const startStr = event.startTime ? formatDateAndTime(event.startTime) : null;
        const endStr = event.endTime ? formatDateAndTime(event.endTime) : null;

        if (startStr && endStr) {
           
            dateLine = `Start: ${startStr} | End: ${endStr}`;
        } else if (startStr) {
           
            dateLine = `Start: ${startStr}`;
        } else if (endStr) {
           
            dateLine = `End: ${endStr}`;
        }
    }

    useEffect(() => {
        if (!event?.id) return;

        const fetchScheduledGames = async () => {
            setIsLoadingGames(true);
            setErrorGames(null);
            setScheduledGames([]);

            try {
                const response = await axios.get(`/events/scheduledevent/${event.id}`);
                setScheduledGames(Array.isArray(response.data) ? response.data : []);
            } catch (err) {
                console.error(err);
                setErrorGames("Could not load games.");
                setScheduledGames([]);
            } finally {
                setIsLoadingGames(false);
            }
        };

        fetchScheduledGames();
    }, [event?.id]);

   
    const isExpired = event.endTime && new Date(event.endTime) < new Date();

    // Unregister
    const handleUnregister = async () => {
        try {
            await axios.delete(`/events/${event.id}/player/${playerId}`);
            
            window.location.reload();
        } catch (error) {
            console.error(error);
            alert("Error unregistering from event.");
        }
    };

    // Cancel
    const handleCancelEvent = async () => {
        try {
            await axios.delete(`/events/${event.id}`);
            
            window.location.reload();
        } catch (error) {
            console.error(error);
            alert("Error canceling event.");
        }
    };

    return (
        <div className={`request-card event-card ${isExpired ? 'expired-event' : ''}`}>
            {isExpired && <span className="expired-badge">Expired</span>}
           
            <div className="request-header">
                <h3 className="event-title">{event.name || 'Unnamed Event'}</h3>
            </div>
           
            <div className="request-info event-info">
                {dateLine && <p className="event-date"><strong>📅</strong> {dateLine}</p>}
                {event.description && <p className="event-description">{event.description}</p>}
            </div>

            <div className="games-section">
                <div className="games-section-title">📋 Scheduled Games:</div>
                {isLoadingGames && <p>Loading games...</p>}
                {errorGames && <p className="error-message">{errorGames}</p>}
                {!isLoadingGames && !errorGames && (
                    scheduledGames.length > 0 ? (
                        <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
                            {scheduledGames.map((game) => (
                                <li key={game.id} className="games-list-item">🎮 {game.name || 'Unnamed Game'}</li>
                            ))}
                        </ul>
                    ) : (
                        <p>No specific games listed for this event.</p>
                    )
                )}
            </div>

            {!isExpired && (
                <div className="event-action-buttons" style={{ marginTop: '1rem' }}>
                    {isCreator ? (
                        <button className="action-button cancel-button" onClick={handleCancelEvent}>
                            Cancel My Event
                        </button>
                    ) : (
                        <button className="action-button" onClick={handleUnregister}>
                            Unregister from Event
                        </button>
                    )}
                </div>
            )}
        </div>
    );
};

function MyEvents() {
    const { user } = useAuth();
    const [events, setEvents] = useState([]);
    const [createdEvents, setCreatedEvents] = useState([]);
    const [playerId, setPlayerId] = useState(null);

    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showCreateEvent, setShowCreateEvent] = useState(false);
    const [newEventName, setNewEventName] = useState("");
    const [newEventDesc, setNewEventDesc] = useState("");
    const [newEventStart, setNewEventStart] = useState("");
    const [newEventEnd, setNewEventEnd] = useState("");
    const [allGames, setAllGames] = useState([]);
    const [selectedGameIds, setSelectedGameIds] = useState([]);

    useEffect(() => {
        const fetchAllGames = async () => {
            try {
                const response = await axios.get("/games");
                if (Array.isArray(response.data)) {
                    setAllGames(response.data);
                }
            } catch (e) {
                console.error("Failed to fetch all games:", e);
            }
        };
        fetchAllGames();
    }, []);

    useEffect(() => {
        const fetchEvents = async () => {
            if (!user?.userId) {
                setIsLoading(false);
                setError("Please log in to view your events.");
                return;
            }

            try {
                const playerRes = await axios.get(`/users/${user.userId}/player-id`);
                const pid = playerRes.data;
                setPlayerId(pid);

                const eventsRes = await axios.get(`/events/player/${pid}`);
                let registeredList = Array.isArray(eventsRes.data) ? eventsRes.data : [];

                try {
                    const createdRes = await axios.get(`/events/bycreator/${pid}`);
                    const createdList = Array.isArray(createdRes.data) ? createdRes.data : [];
                    setCreatedEvents(createdList);

                    registeredList = registeredList.filter(
                        (regEvent) => !createdList.some((ce) => ce.id === regEvent.id)
                    );
                } catch (e) {
                    console.error("Failed to load created events:", e);
                }

                setEvents(registeredList);
            } catch (err) {
                console.error(err);
                setError("Failed to load your events.");
                setEvents([]);
                setCreatedEvents([]);
            } finally {
                setIsLoading(false);
            }
        };

        fetchEvents();
    }, [user?.userId]);

   
    const sortedEvents = [...events].sort((a, b) => {
        const now = new Date();
        const aExpired = a.endTime && new Date(a.endTime) < now;
        const bExpired = b.endTime && new Date(b.endTime) < now;

        if (aExpired && !bExpired) return 1;
        if (!aExpired && bExpired) return -1;
        return 0;
    });

   
    const sortedCreatedEvents = [...createdEvents].sort((a, b) => {
        const now = new Date();
        const aExpired = a.endTime && new Date(a.endTime) < now;
        const bExpired = b.endTime && new Date(b.endTime) < now;

        if (aExpired && !bExpired) return 1;
        if (!aExpired && bExpired) return -1;
        return 0;
    });

    const handleCreateEvent = async () => {
        if (!newEventName.trim()) {
          alert("Please provide an event name.");
          return;
        }
        try {
          const createRes = await axios.post("/events", {
            name: newEventName,
            description: newEventDesc,
            startTime: newEventStart ? new Date(newEventStart) : null,
            endTime: newEventEnd ? new Date(newEventEnd) : null
          });
          const newEvent = createRes.data;
      
          if (selectedGameIds.length > 0) {
            await axios.post(`/events/${newEvent.id}/scheduleGames`, selectedGameIds);
          }
      
          if (playerId) {
            await axios.post(`/events/${newEvent.id}/player/${playerId}`);
          }
      
      
          setShowCreateEvent(false);
          window.location.reload();
        } catch (error) {
          console.error("Error creating event:", error);
          alert("Failed to create event. Check console for details.");
        }
      };
      

    const handleCancelCreate = () => {
        setShowCreateEvent(false);
        setNewEventName("");
        setNewEventDesc("");
        setNewEventStart("");
        setNewEventEnd("");
        setSelectedGameIds([]);
    };

    const toggleSelectedGame = (gameId) => {
        if (selectedGameIds.includes(gameId)) {
            setSelectedGameIds(selectedGameIds.filter((id) => id !== gameId));
        } else {
            setSelectedGameIds([...selectedGameIds, gameId]);
        }
    };

    return (
        <div className="my-events-container" style={{ padding: '30px', position: 'relative', backgroundColor: '#F4F4F4' }}>
            {isLoading && <p className="centered loading-message">Loading your events...</p>}
            {error && <p className="centered error-message">{error}</p>}

            {!isLoading && !error && !showCreateEvent && (
                <button
                    className="btn success create-event-btn"
                    onClick={() => setShowCreateEvent(true)}
                >
                    <AddIcon sx={{ color: 'inherit' }} /> Create Event
                </button>
            )}

            {showCreateEvent && (
                <div className="modal-backdrop">
                    <div className="modal-content">
                        <h2 className="modal-title">Create New Event</h2>
                       
                        <div className="form-group">
                            <label className="form-label">Event Name:</label>
                            <input
                                type="text"
                                className="form-input"
                                value={newEventName}
                                onChange={(e) => setNewEventName(e.target.value)}
                                placeholder="Enter event name..."
                            />
                        </div>
                       
                        <div className="form-group">
                            <label className="form-label">Description:</label>
                            <textarea
                                className="form-input form-textarea"
                                value={newEventDesc}
                                onChange={(e) => setNewEventDesc(e.target.value)}
                                placeholder="Describe your event..."
                            />
                        </div>
                       
                        <div className="form-group">
                            <label className="form-label">Start Time:</label>
                            <input
                                type="datetime-local"
                                className="form-input"
                                value={newEventStart}
                                onChange={(e) => setNewEventStart(e.target.value)}
                            />
                        </div>
                       
                        <div className="form-group">
                            <label className="form-label">End Time:</label>
                            <input
                                type="datetime-local"
                                className="form-input"
                                value={newEventEnd}
                                onChange={(e) => setNewEventEnd(e.target.value)}
                            />
                        </div>

                        <div className="games-selection">
                            <div className="games-selection-title">Select Games to Schedule:</div>
                            <div className="games-list">
                                {allGames.length > 0 ? (
                                    allGames.map((game) => (
                                        <div key={game.id} className="game-option">
                                            <label style={{ display: 'flex', alignItems: 'center' }}>
                                                <input
                                                    type="checkbox"
                                                    className="game-checkbox"
                                                    checked={selectedGameIds.includes(game.id)}
                                                    onChange={() => toggleSelectedGame(game.id)}
                                                />
                                                {game.name}
                                            </label>
                                        </div>
                                    ))
                                ) : (
                                    <p>No games available</p>
                                )}
                            </div>
                        </div>

                        <div className="modal-actions">
                            <button className="btn btn-secondary" onClick={handleCancelCreate}>
                                Cancel
                            </button>
                            <button className="btn btn-primary" onClick={handleCreateEvent}>
                                Create Event
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {!isLoading && !error && (
                <>
                    <h2 className="centered event-section-title">
                        Events You Created
                    </h2>
                    {createdEvents.length > 0 ? (
                        <div className="event-list-container">
                            {sortedCreatedEvents.map((event) => (
                                <EventCard
                                    key={event.id}
                                    event={event}
                                    playerId={playerId}
                                    isCreator
                                />
                            ))}
                        </div>
                    ) : (
                        <p className="centered empty-message">You haven't created any events yet.</p>
                    )}

                    <hr className="divider" />

                    <h2 className="centered event-section-title">
                        Events You Are Registered For
                    </h2>
                    {sortedEvents.length > 0 ? (
                        <div className="event-list-container">
                            {sortedEvents.map((event) => (
                                <EventCard
                                    key={event.id}
                                    event={event}
                                    playerId={playerId}
                                    isCreator={false}
                                />
                            ))}
                        </div>
                    ) : (
                        <p className="centered empty-message">You are not registered for any events.</p>
                    )}
                </>
            )}

        </div>
    );
}

export default MyEvents;