import React, { useState, useEffect } from 'react';
import axios from 'axios';

import { useAuth } from '../../AuthContext';
import '../../styles/layout.css';
import '../../styles/event-history.css';
import { Box, CircularProgress } from '@mui/material';


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
                                <li key={game.id} className="games-list-item">
                                    🎮 {game.name || 'Unnamed Game'}
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p>No specific games listed for this event.</p>
                    )
                )}
            </div>

            {}
        </div>
    );
};

function EventHistory() {
    const { user } = useAuth();
    const [events, setEvents] = useState([]);
    const [createdEvents, setCreatedEvents] = useState([]);
    const [playerId, setPlayerId] = useState(null);
    const [loading, setLoading] = useState(true);

    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchEvents = async () => {
            if (!user?.userId) {

                setLoading(false);
                setError("Please log in to view your event history.");

                return;
            }

            try {
                const playerRes = await axios.get(`/users/${user.userId}/player-id`);

                const pid = playerRes.data;
                setPlayerId(pid);

                // Fetch all events the user is registered for
                const eventsRes = await axios.get(`/events/player/${pid}`);
                let registeredList = Array.isArray(eventsRes.data) ? eventsRes.data : [];

                try {
                    // Fetch events the user created
                    const createdRes = await axios.get(`/events/bycreator/${pid}`);
                    const createdList = Array.isArray(createdRes.data) ? createdRes.data : [];
                    setCreatedEvents(createdList);

                    // Remove created events from registered list so they don’t appear twice
                    registeredList = registeredList.filter(
                        (regEvent) => !createdList.some((ce) => ce.id === regEvent.id)
                    );
                } catch (e) {
                    console.error("Failed to load created events:", e);
                }

                setEvents(registeredList);
            } catch (err) {
                console.error(err);
                setError("Failed to load your event history.");
                setEvents([]);
                setCreatedEvents([]);
            } finally {
                setLoading(false);

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

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
                <CircularProgress className="custom-progress" />
            </Box>
        );
    }

    return (
        <div className="my-events-container">
            <h2 className="centered event-section-title">
                Events You Created
            </h2>
            {sortedCreatedEvents.length > 0 ? (
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
        </div>
    );
}

export default MyEvents;

