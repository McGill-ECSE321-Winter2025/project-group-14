import React, { useState, useEffect } from 'react';
import '../../styles/EventCard.css';
import { CircularProgress } from '@mui/material';

const BorrowedGameCard = ({ request }) => {
    const [expanded, setExpanded] = useState(false);
    const [imageUrl, setImageUrl] = useState(null);
    const [imageLoading, setImageLoading] = useState(true);
    const [imageError, setImageError] = useState(false);

    const {
        gameId,
        gameName = "Untitled Game",
        senderName = "Unknown",
        startTime,
        endTime,
    } = request;

    useEffect(() => {
        const fetchGameImage = async () => {
            try {
                if (!gameId) {
                    setImageError(true);
                    return;
                }
                const response = await fetch(`http://localhost:8080/games/${gameId}/image`);
                if (response.ok) {
                    const imageBlob = await response.blob();
                    const url = URL.createObjectURL(imageBlob);
                    setImageUrl(url);
                } else {
                    setImageError(true);
                }
            } catch (error) {
                console.error("Error fetching game image:", error);
                setImageError(true);
            } finally {
                setImageLoading(false);
            }
        };

        fetchGameImage();

        return () => {
            if (imageUrl) URL.revokeObjectURL(imageUrl);
        };
    }, [gameId]);

    const toggleExpand = () => setExpanded((prev) => !prev);

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
                <div
                    className="event-details"
                    style={{
                        display: 'grid',
                        gridTemplateColumns: '160px 1fr',
                        gap: '200px',
                        alignItems: 'center',
                        padding: '16px',
                        boxSizing: 'border-box',
                    }}
                >
                    {/* Info Section */}
                    <div className="borrow-info">
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
                    {/* Image Section */}
                    <div style={{
                        width: '160px',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                    }}>
                        {imageLoading ? (
                            <CircularProgress size={32} />
                        ) : (
                            <img
                                src={imageError ? '/default-game-image.jpg' : imageUrl}
                                alt={`${gameName} cover`}
                                style={{
                                    width: '100%',
                                    maxHeight: '160px',
                                    objectFit: 'contain',
                                    borderRadius: '12px',
                                    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
                                }}
                            />
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default BorrowedGameCard;
