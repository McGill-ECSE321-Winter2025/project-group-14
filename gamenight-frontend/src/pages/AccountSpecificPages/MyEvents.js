
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../../AuthContext';
import '../../styles/layout.css';
import '../../styles/event-history.css';

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
            alert("You have unregistered from this event.");
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
            alert("Event canceled successfully!");
            window.location.reload();
        } catch (error) {
            console.error(error);
            alert("Error canceling event.");
        }
    };

    return (
        <div className="request-card">
            <div className="request-header">
                <h3>{event.name || 'Unnamed Event'}</h3>
            </div>
            <div className="request-info">
                {}
                {dateLine && <p><strong>Date: </strong>{dateLine}</p>}
                {event.description && <p>{event.description}</p>}
            </div>

            <div
                className="scheduled-games-section"
                style={{
                    marginTop: '1rem',
                    paddingTop: '0.5rem',
                    borderTop: '1px solid #eee',
                }}
            >
                <strong>Scheduled Games:</strong>
                {isLoadingGames && <p>Loading games...</p>}
                {errorGames && <p className="error-message">{errorGames}</p>}
                {!isLoadingGames && !errorGames && (
                    scheduledGames.length > 0 ? (
                        <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
                            {scheduledGames.map((game) => (
                                <li key={game.id}>{game.name || 'Unnamed Game'}</li>
                            ))}
                        </ul>
                    ) : (
                        <p>No specific games listed for this event.</p>
                    )
                )}
            </div>

            {}
            {!isExpired && (
                <div className="event-action-buttons" style={{ marginTop: '1rem' }}>
                    {isCreator ? (
                        <button className="unregister-btn" onClick={handleCancelEvent}>
                            Cancel My Event
                        </button>
                    ) : (
                        <button className="unregister-btn" onClick={handleUnregister}>
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

            alert("Event created successfully!");
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
        <div className="my-events-container" style={{ padding: '20px', position: 'relative' }}>
            {isLoading && <p className="centered">Loading your events...</p>}
            {error && <p className="centered error-message">{error}</p>}

            {!isLoading && !error && (
                <button
                    style={{ position: 'absolute', top: 20, right: 20 }}
                    className="unregister-btn"
                    onClick={() => setShowCreateEvent(true)}
                >
                    Create Event
                </button>
            )}

            {showCreateEvent && (
                <div
                    style={{
                        position: 'fixed',
                        top: 0,
                        left: 0,
                        width: '100%',
                        height: '100%',
                        backgroundColor: 'rgba(0,0,0,0.5)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        zIndex: 9999,
                        overflow: 'auto'
                    }}
                >
                    <div
                        style={{
                            backgroundColor: '#fff',
                            padding: '20px',
                            borderRadius: '8px',
                            width: '500px',
                            maxWidth: '90%',
                            maxHeight: '90vh',
                            overflow: 'auto'
                        }}
                    >
                        <h2 style={{ marginTop: 0 }}>Create New Event</h2>
                        <label>
                            Name:
                            <input
                                type="text"
                                value={newEventName}
                                onChange={(e) => setNewEventName(e.target.value)}
                                style={{ width: '100%', marginBottom: '1rem' }}
                            />
                        </label>
                        <label>
                            Description:
                            <textarea
                                value={newEventDesc}
                                onChange={(e) => setNewEventDesc(e.target.value)}
                                style={{ width: '100%', marginBottom: '1rem', minHeight: '80px' }}
                            />
                        </label>
                        <label>
                            Start Time:
                            <input
                                type="datetime-local"
                                value={newEventStart}
                                onChange={(e) => setNewEventStart(e.target.value)}
                                style={{ width: '100%', marginBottom: '1rem' }}
                            />
                        </label>
                        <label>
                            End Time:
                            <input
                                type="datetime-local"
                                value={newEventEnd}
                                onChange={(e) => setNewEventEnd(e.target.value)}
                                style={{ width: '100%', marginBottom: '1rem' }}
                            />
                        </label>

                        <div style={{ marginBottom: '1rem' }}>
                            <strong>Select Games to Schedule:</strong>
                            <div
                                style={{
                                    maxHeight: '200px',
                                    overflowY: 'auto',
                                    border: '1px solid #ccc',
                                    marginTop: '0.5rem',
                                    padding: '10px'
                                }}
                            >
                                {allGames.length > 0 ? (
                                    allGames.map((game) => (
                                        <div key={game.id} style={{ padding: '5px 0' }}>
                                            <label style={{ display: 'flex', alignItems: 'center' }}>
                                                <input
                                                    type="checkbox"
                                                    checked={selectedGameIds.includes(game.id)}
                                                    onChange={() => toggleSelectedGame(game.id)}
                                                    style={{ marginRight: '8px' }}
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

                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
                            <button className="unregister-btn" onClick={handleCancelCreate}>
                                Cancel
                            </button>
                            <button className="unregister-btn" onClick={handleCreateEvent}>
                                Create
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {!isLoading && !error && (
                <>
                    <h2 className="centered" style={{ marginBottom: '1rem' }}>
                        Events You Created
                    </h2>
                    {createdEvents.length > 0 ? (
                        <div className="event-list-container">
                            {createdEvents.map((event) => (
                                <EventCard
                                    key={event.id}
                                    event={event}
                                    playerId={playerId}
                                    isCreator
                                />
                            ))}
                        </div>
                    ) : (
                        <p className="centered">You haven't created any events.</p>
                    )}

                    <hr style={{ margin: '2rem auto', width: '60%' }} />

                    <h2 className="centered" style={{ marginBottom: '1rem' }}>
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
                        <p className="centered">You are not registered for any events.</p>
                    )}
                </>
            )}
        </div>
    );
}

export default MyEvents;
