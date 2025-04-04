import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../../AuthContext.js';
import '../../styles/layout.css';
import '../../styles/event-history.css';
import EventCard from '../../components/cards/EventCard.js'; // ✅ relative to MyEvents.js


function MyEvents() {
    const { user } = useAuth();
    const [events, setEvents] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
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
