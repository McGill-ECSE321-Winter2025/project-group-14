import React, { useState, useEffect } from 'react';
import axios from 'axios';
import PropTypes from 'prop-types';

const formatDateAndTime = (dateString) => {
    if (!dateString) return '';
    try {
        const d = new Date(dateString);
        if (isNaN(d.getTime())) {
             console.warn("Invalid date string received:", dateString);
             return 'Invalid Date';
        }
        const datePart = d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
        const timePart = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        return `${datePart} ${timePart}`;
    } catch (error) {
        console.error("Error formatting date:", dateString, error);
        return 'Invalid Date';
    }
};


function ClickableEventCard({ event, playerId, isCreator, showPopup }) {
    const [isExpanded, setIsExpanded] = useState(false);
    const [scheduledGames, setScheduledGames] = useState([]);
    const [isLoadingGames, setIsLoadingGames] = useState(false);
    const [errorGames, setErrorGames] = useState(null);

    const isExpired = event.endTime && new Date(event.endTime) < new Date();

    let dateLine = "";
    const startStr = event.startTime ? formatDateAndTime(event.startTime) : null;
    const endStr = event.endTime ? formatDateAndTime(event.endTime) : null;

    if (startStr && endStr) {
        dateLine = `Starts: ${startStr} | Ends: ${endStr}`;
    } else if (startStr) {
        dateLine = `Starts: ${startStr}`;
    } else if (endStr) {
        dateLine = `Ends: ${endStr}`;
    } else {
        dateLine = "Date not specified";
    }

    useEffect(() => {
        if (isExpanded && event?.id && !scheduledGames.length && !isLoadingGames && !errorGames) {
            const fetchScheduledGames = async () => {
                setIsLoadingGames(true);
                setErrorGames(null);
                try {
                    const response = await axios.get(`/events/scheduledevent/${event.id}`);
                    setScheduledGames(Array.isArray(response.data) ? response.data : []);
                } catch (err) {
                    console.error("Error fetching scheduled games:", err);
                    setErrorGames("Could not load games.");
                    setScheduledGames([]);
                } finally {
                    setIsLoadingGames(false);
                }
            };
            fetchScheduledGames();
        }
    }, [isExpanded, event?.id, scheduledGames.length, isLoadingGames, errorGames]);


    const toggleExpand = () => {
        setIsExpanded(!isExpanded);
    };

    const handleCancelEvent = async (e) => {
        e.stopPropagation();
        if (!isCreator || !event?.id) return;

        try {
            await axios.delete(`/events/${event.id}`);
            showPopup("Event cancelled successfully.", "success");
            window.location.reload();
        } catch (error) {
            console.error("Error canceling event:", error);
            const errorMsg = error.response?.data?.message || error.message;
            showPopup(`Error canceling event: ${errorMsg}`, "error");
        }
    };

    return (
        <div
            className={`event-card ${isExpired ? 'expired-event' : ''}`}
            onClick={toggleExpand}
            role="button"
            tabIndex="0"
            onKeyPress={(e) => (e.key === 'Enter' || e.key === ' ') && toggleExpand()}
            aria-expanded={isExpanded}
        >
            {isExpired && <span className="expired-badge">Expired</span>}

            <div className="event-header">
                <div className="event-summary">
                    <h3>{event.name || 'Unnamed Event'}</h3>
                    <p className="event-date">📅 {dateLine}</p>
                </div>
                <span className={`chevron ${isExpanded ? 'expanded' : ''}`}>▼</span>
            </div>

             {!isExpanded && event.description && (
                 <p className="description-summary" style={{ marginTop: '0.5rem', fontSize: '0.9rem', color: '#555' }}>
                    {event.description.length > 100 ? `${event.description.substring(0, 97)}...` : event.description}
                 </p>
             )}

            {isExpanded && (
                <div className="event-details">
                     {event.description && (
                        <div className="section description-section">
                             <h4>Description</h4>
                             <p className="description">{event.description}</p>
                        </div>
                    )}

                    <div className="section games-section">
                        <h4>📋 Scheduled Games</h4>
                        {isLoadingGames && <p className="loading">Loading games...</p>}
                        {errorGames && <p className="error-message">{errorGames}</p>}
                        {!isLoadingGames && !errorGames && (
                            scheduledGames.length > 0 ? (
                                <ul className="games-list">
                                    {scheduledGames.map((game) => (
                                        <li key={game.id}>🎮 {game.name || 'Unnamed Game'}</li>
                                    ))}
                                </ul>
                            ) : (
                                <p className="empty">No specific games listed for this event.</p>
                            )
                        )}
                    </div>

                    {isCreator && !isExpired && (
                        <div className="event-action-buttons" style={{ marginTop: '1rem', textAlign: 'right' }}>
                            <button
                                className="btn danger"
                                onClick={handleCancelEvent}
                            >
                                Cancel My Event
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

ClickableEventCard.propTypes = {
    event: PropTypes.shape({
        id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
        name: PropTypes.string,
        description: PropTypes.string,
        startTime: PropTypes.string,
        endTime: PropTypes.string,
    }).isRequired,
    playerId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    isCreator: PropTypes.bool.isRequired,
    showPopup: PropTypes.func.isRequired,
};

export default ClickableEventCard;