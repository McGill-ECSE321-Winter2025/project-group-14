import React, { useState, useEffect } from 'react';
import '../../styles/EventCard.css';
import { CircularProgress } from '@mui/material';
import { GameHistoryAPI } from '../../GettingAllGamesAPI';

const BorrowedGameCard = ({ request }) => {
    const [expanded, setExpanded] = useState(false);
    const [imageUrl, setImageUrl] = useState(null);
    const [imageLoading, setImageLoading] = useState(true);
    const [imageError, setImageError] = useState(false);
    const [gameId, setGameId] = useState(null);

    const {
        id: borrowingRequestId,
        gameCopyId,
        gameName = "Untitled Game",
        senderName = "Unknown",
        startTime,
        endTime
    } = request;

    useEffect(() => {
        const fetchGameId = async () => {
            try {
                const gameCopy = await GameHistoryAPI.getGameCopyById(gameCopyId);
                if (gameCopy?.game?.id) {
                    setGameId(gameCopy.game.id);
                } else {
                    throw new Error("No game found inside game copy response");
                }
            } catch (err) {
                console.error("Failed to fetch game ID from game copy:", err);
                setImageError(true);
                setImageLoading(false);
            }
        };

        fetchGameId();
    }, [gameCopyId]);

    useEffect(() => {
        if (!gameId) return;

        let objectUrl = null;

        const fetchImage = async () => {
            try {
                const response = await fetch(`http://localhost:8080/games/${gameId}/image`);
                if (!response.ok) throw new Error("Image not found");
                const imageBlob = await response.blob();
                objectUrl = URL.createObjectURL(imageBlob);
                setImageUrl(objectUrl);
            } catch (error) {
                console.error("Failed to fetch image blob:", error);
                setImageError(true);
            } finally {
                setImageLoading(false);
            }
        };

        fetchImage();

        return () => {
            if (objectUrl) URL.revokeObjectURL(objectUrl);
        };
    }, [gameId]);

    const toggleExpand = () => setExpanded(prev => !prev);

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
                        gap: '32px',
                        alignItems: 'center',
                        padding: '16px',
                        boxSizing: 'border-box',
                    }}
                >
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
                </div>
            )}
        </div>
    );
};

export default BorrowedGameCard;
