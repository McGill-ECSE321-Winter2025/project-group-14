import React, { useState } from 'react';
import axios from 'axios';
import '../../styles/EventCard.css';

const EventCard = ({ event }) => {
    const [expanded, setExpanded] = useState(false);
    const [loading, setLoading] = useState(false);
    const [players, setPlayers] = useState([]);
    const [games, setGames] = useState([]);

    const toggleExpand = async () => {
        if (!expanded) {
            setLoading(true);
            try {
                const [playersRes, gamesRes] = await Promise.all([
                    axios.get(`/events/${event.id}/players`),
                    axios.get(`/events/scheduledevent/${event.id}`)
                ]);
                setPlayers(playersRes.data || []);
                setGames(gamesRes.data || []);
            } catch (error) {
                console.error("Error loading event details:", error);
            } finally {
                setLoading(false);
            }
        }
        setExpanded(!expanded);
    };

    const eventDate = new Date(event.startTime).toLocaleDateString();
    const eventTime = new Date(event.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    return (
        <div className={`event-card ${expanded ? 'expanded' : ''}`} onClick={toggleExpand}>
            <div className="event-summary">
                <div className="event-header">
                    <div>
                        <h3>{event.name}</h3>
                        <p>{eventDate} @ {eventTime}</p>
                    </div>
                    <div className={`chevron ${expanded ? 'expanded' : ''}`}>&#x25BC;</div>
                </div>
            </div>

            {expanded && (
                <div className="event-details">
                    {loading ? (
                        <p className="loading">Loading details...</p>
                    ) : (
                        <>
                            {event.description && <p className="description">{event.description}</p>}

                            <div className="section">
                                <strong>Participants:</strong>
                                <div className="players-list">
                                    {players.length > 0 ? players.map(p => (
                                        <span key={p.id} className="player-pill">{p.name}</span>
                                    )) : <p className="empty">No players registered.</p>}
                                </div>
                            </div>

                            <div className="section">
                                <strong>Scheduled Games:</strong>
                                <ul className="games-list">
                                    {games.length > 0 ? games.map(g => (
                                        <li key={g.id}>{g.name || 'Unnamed Game'}</li>
                                    )) : <p className="empty">No games listed.</p>}
                                </ul>
                            </div>
                        </>
                    )}
                </div>
            )}
        </div>
    );
};

export default EventCard;
