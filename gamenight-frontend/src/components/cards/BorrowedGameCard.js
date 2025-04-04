import React, { useState } from 'react';
import '../../styles/EventCard.css';

const BorrowedGameCard = ({ request }) => {
    const [expanded, setExpanded] = useState(false);

    const {
        gameName = "Untitled Game",
        senderName = "Unknown",
        startTime,
        endTime,
        gameImageUrl = "https://mixmag.net/assets/uploads/images/_columns2/shrekrave2.png"//"/default-game-image.png"
    } = request;

    const toggleExpand = () => setExpanded(!expanded);

    const start = new Date(startTime);
    const end = new Date(endTime);
    const duration = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
    const formattedDate = start.toLocaleDateString();
    const formattedTime = start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    return (
        <div className={`event-card ${expanded ? 'expanded' : ''}`} onClick={toggleExpand}>
            <div className="event-summary">
                <div className="event-header">
                    <div>
                        <h3>{gameName}</h3>
                        <p>{formattedDate} @ {formattedTime}</p>
                    </div>
                    <div className={`chevron ${expanded ? 'expanded' : ''}`}>&#x25BC;</div>
                </div>
            </div>

            {expanded && (
                <div className="event-details" style={{ display: 'flex', flexWrap: 'wrap', gap: '20px', alignItems: 'center', justifyContent: 'v=ce=' }}>
                    <div className="borrow-info" style={{ flex: '1 1 300px' }}>
                        <div className="section">
                            <strong>Borrow Duration:</strong>
                            <p>{duration} day{duration !== 1 ? 's' : ''}</p>
                        </div>

                        <div className="section">
                            <strong>Borrow Period:</strong>
                            <p>From {start.toLocaleDateString()} to {end.toLocaleDateString()}</p>
                        </div>

                        <div className="section">
                            <strong>Owner:</strong>
                            <p>{senderName}</p>
                        </div>
                    </div>

                    <div className="central-image-container" style={{
                        flex: '0 0 160px',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        margin: 'auto'
                    }}>
                        <img
                            src={gameImageUrl}
                            alt={`${gameName} cover`}
                            className="central-image"
                            style={{
                                height: '150px',
                                borderRadius: '12px',
                                boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
                                objectFit: 'contain'
                            }}
                        />
                    </div>
                </div>
            )}
        </div>
    );
};

export default BorrowedGameCard;