import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../../AuthContext';
import '../../styles/layout.css';
import '../../styles/event-history.css';

const EventCard = ({ event }) => {
    const [scheduledGames, setScheduledGames] = useState([]);
    const [isLoadingGames, setIsLoadingGames] = useState(false);
    const [errorGames, setErrorGames] = useState(null);

    const eventDate = event.startTime ? new Date(event.startTime).toLocaleDateString() : 'N/A';
    const eventTime = event.startTime ? new Date(event.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '';

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

    return (
        <div className="request-card">
            <div className="request-header">
                <h3>{event.name || 'Unnamed Event'}</h3>
            </div>
            <div className="request-info">
                <p><strong>Date:</strong> {eventDate} {eventTime}</p>
                {event.description && <p>{event.description}</p>}
            </div>

            <div className="scheduled-games-section" style={{ marginTop: '1rem', paddingTop: '0.5rem', borderTop: '1px solid #eee' }}>
                <strong>Scheduled Games:</strong>
                {isLoadingGames && <p>Loading games...</p>}
                {errorGames && <p className="error-message">{errorGames}</p>}
                {!isLoadingGames && !errorGames && (
                    scheduledGames.length > 0 ? (
                        <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
                            {scheduledGames.map(game => (
                                <li key={game.id}>{game.name || 'Unnamed Game'}</li>
                            ))}
                        </ul>
                    ) : (
                        <p>No specific games listed for this event.</p>
                    )
                )}
            </div>
        </div>
    );
};

function MyEvents() {
    const { user } = useAuth(); // ✅ This is where `user` is defined
    const [events, setEvents] = useState([]);
    const [isLoading, setIsLoading] = useState(true); // ✅ Declare state
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchEvents = async () => {
            if (!user?.userId) {
                setIsLoading(false);
                setError("Please log in to view your events.");
                return;
            }
    
            try {
                const playerRes = await axios.get(`/users/${user.userId}/player-id`);
                const playerId = playerRes.data;

                const eventsRes = await axios.get(`/events/player/${playerId}`);
                if (Array.isArray(eventsRes.data)) {
                    setEvents(eventsRes.data);
                } else {
                    setEvents([]);
                }
            } catch (err) {
                console.error(err);
                setError("Failed to load your events.");
                setEvents([]);
            } finally {
                setIsLoading(false);
            }
        };
    
        fetchEvents();
    }, [user?.userId]);
    
    return (
        <div className="my-events-container" style={{ padding: '20px' }}>
            {isLoading && <p className="centered">Loading your events...</p>}
            {error && <p className="centered error-message">{error}</p>}
            {!isLoading && !error && (
                events.length > 0 ? (
                    <div className="event-list-container">
                        {events.map(event => (
                            <EventCard key={event.id} event={event} />
                        ))}
                    </div>
                ) : (
                    <p className="centered">You are not registered for any events.</p>
                )
            )}
        </div>
    );
}

export default MyEvents;
