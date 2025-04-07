import React, { useState, useEffect } from 'react';
import dayjs from 'dayjs'; // Import dayjs
import '../../styles/EventCard.css';
import { CircularProgress } from '@mui/material';
import { GameHistoryAPI } from '../../GettingAllGamesAPI';

// Day.js date formatting function
const formatDate = (dateString) => {
    if (!dateString) return '';
    try {
        const date = dayjs(dateString);
        if (!date.isValid()) {
            console.warn("[formatDate] Invalid date string received:", dateString);
            return 'Invalid Date';
        }
        return date.format('MMM D, YYYY'); // Format: "Jan 1, 2023"
    } catch (error) {
        console.error("[formatDate] Error formatting date:", dateString, error);
        return 'Invalid Date';
    }
};

const BorrowedGameCard = ({ request }) => {
    const [expanded, setExpanded] = useState(false);
    const [imageUrl, setImageUrl] = useState(null);
    const [imageLoading, setImageLoading] = useState(true);
    const [imageError, setImageError] = useState(false);
    const [gameId, setGameId] = useState(null);
    const { senderName = "Unknown" } = request;
    const [ownerName, setOwnerName] = useState(senderName);

    const {
        gameCopyId,
        gameName = "Untitled Game",
        startTime,
        endTime
    } = request;

    useEffect(() => {
        const fetchGameCopyData = async () => {
            if (!gameCopyId) {
                console.error("GameCopy ID is missing");
                setImageError(true);
                setImageLoading(false);
                return;
            }
            try {
                const gameCopy = await GameHistoryAPI.getGameCopyById(gameCopyId);
                if (!gameCopy) {
                    throw new Error('No game copy returned for ID: ' + gameCopyId);
                }

                if (gameCopy.game?.id) {
                    setGameId(gameCopy.game.id);
                } else {
                    console.warn("No game ID found for GameCopy ID:", gameCopyId);
                    setImageError(true);
                    setImageLoading(false);
                }

                if (gameCopy.gameOwnerName) {
                    setOwnerName(gameCopy.gameOwnerName);
                } else {
                     console.warn("No owner name found, using senderName");
                }

            } catch (err) {
                console.error('Failed to fetch game copy data:', gameCopyId, err);
                setImageError(true);
                setImageLoading(false);
            }
        };

        fetchGameCopyData();
    }, [gameCopyId]);

    useEffect(() => {
        if (!gameId || imageError) {
            if (!gameId) setImageLoading(false);
            return;
        }

        let objectUrl = null;

        const fetchImage = async () => {
            setImageLoading(true);
            setImageError(false);
            setImageUrl(null);

            try {
                const response = await fetch(`http://localhost:8080/games/${gameId}/image`);
                if (!response.ok) {
                     if (response.status === 404) {
                        console.log(`Image not found for game ID ${gameId}`);
                        setImageError(true);
                     } else {
                         throw new Error(`HTTP error ${response.status}`);
                     }
                } else {
                    const imageBlob = await response.blob();
                    objectUrl = URL.createObjectURL(imageBlob);
                    setImageUrl(objectUrl);
                    setImageError(false);
                }
            } catch (error) {
                console.error(`Failed to fetch image for game ID ${gameId}:`, error);
                setImageError(true);
            } finally {
                setImageLoading(false);
            }
        };

        fetchImage();

        return () => {
            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
            }
        };
    }, [gameId]);

    const toggleExpand = () => setExpanded(prev => !prev);

    // Calculate duration using dayjs
    const duration = startTime && endTime 
        ? dayjs(endTime).diff(dayjs(startTime), 'day')
        : null;

    // Format dates using dayjs
    const formattedStartDate = formatDate(startTime);
    const formattedEndDate = formatDate(endTime);
    const headerDate = formattedStartDate || 'Date not specified';

    return (
        <div className={`event-card ${expanded ? 'expanded' : ''}`} onClick={toggleExpand}>
            <div className="event-summary">
                <div className="event-header">
                    <div>
                        <h3>{gameName}</h3>
                        <p>📅 {headerDate}</p>
                    </div>
                    <div className={`chevron ${expanded ? 'expanded' : ''}`}>&#x25BC;</div>
                </div>
            </div>

            {expanded && (
                <div className="event-details" style={{ display: 'grid', gridTemplateColumns: '160px 1fr', gap: '32px' }}>
                    <div style={{ width: '160px', height: '160px', display: 'flex', justifyContent: 'center', alignItems: 'center', overflow: 'hidden', borderRadius: '12px', backgroundColor: '#f0f0f0' }}>
                        {imageLoading ? (
                            <CircularProgress size={32} />
                        ) : (
                            <img
                                src={imageError ? '/default-game-image.jpg' : imageUrl}
                                alt={`${gameName} cover`}
                                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            />
                        )}
                    </div>

                    <div className="borrow-info">
                        {duration !== null && (
                             <div className="section">
                                <strong>Borrow Duration:</strong>
                                <p>{duration} day{duration !== 1 ? 's' : ''}</p>
                            </div>
                        )}
                        <div className="section">
                            <strong>📅 Borrow Period:</strong>
                             {formattedStartDate && formattedEndDate ? (
                                <p>From {formattedStartDate} to {formattedEndDate}</p>
                            ) : formattedStartDate ? (
                                <p>Starts: {formattedStartDate}</p>
                            ) : formattedEndDate ? (
                                <p>Ends: {formattedEndDate}</p>
                            ) : (
                                <p>Date not specified</p>
                            )}
                        </div>
                        <div className="section">
                            <strong>Owner:</strong>
                            <p>{ownerName}</p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default BorrowedGameCard;