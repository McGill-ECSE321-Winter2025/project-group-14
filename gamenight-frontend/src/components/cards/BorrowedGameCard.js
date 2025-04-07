import React, { useState, useEffect } from 'react';
import '../../styles/EventCard.css'; // Assuming EventCard styles are applicable/shared
import { CircularProgress } from '@mui/material';
import { GameHistoryAPI } from '../../GettingAllGamesAPI'; // Assuming this API exists

// Define the date formatting utility function (outside the component)
const formatDate = (dateString) => {
    if (!dateString) return ''; // Return empty string if input is null/undefined
    try {
        const d = new Date(dateString);
        // Check if the date is valid
        if (isNaN(d.getTime())) {
             console.warn("[formatDate] Invalid date string received:", dateString);
             return 'Invalid Date';
        }
        // Format date only
        return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
    } catch (error) {
        // Log error if formatting fails
        console.error("[formatDate] Error formatting date:", dateString, error);
        return 'Invalid Date'; // Return fallback string
    }
};

const BorrowedGameCard = ({ request }) => {
    const [expanded, setExpanded] = useState(false);
    const [imageUrl, setImageUrl] = useState(null);
    const [imageLoading, setImageLoading] = useState(true);
    const [imageError, setImageError] = useState(false);
    const [gameId, setGameId] = useState(null);
    // Destructure senderName prop for default owner name
    const { senderName = "Unknown" } = request;
    const [ownerName, setOwnerName] = useState(senderName); // Initialize with senderName

    // Destructure other props from request
    const {
        gameCopyId,
        gameName = "Untitled Game",
        startTime,
        endTime
    } = request;

    // --- useEffect hook to fetch GameCopy data (Game ID and Owner Name) ---
    useEffect(() => {
        const fetchGameCopyData = async () => {
            // Check if gameCopyId is provided
            if (!gameCopyId) {
                console.error("GameCopy ID is missing, cannot fetch game copy data.");
                setImageError(true); // Cannot fetch image without gameId from copy
                setImageLoading(false);
                return;
            }
            try {
                // Fetch game copy details
                const gameCopy = await GameHistoryAPI.getGameCopyById(gameCopyId);
                if (!gameCopy) {
                    throw new Error('No game copy returned from API for ID: ' + gameCopyId);
                }

                // Set Game ID if available (needed for image fetching)
                let foundGameId = false;
                if (gameCopy.game?.id) {
                    setGameId(gameCopy.game.id);
                    foundGameId = true;
                } else {
                    console.warn("No game ID found in game copy response for GameCopy ID:", gameCopyId);
                    // If gameId is missing, we cannot fetch the image
                    setImageError(true);
                    setImageLoading(false);
                }

                // Set Owner Name if available from the response
                if (gameCopy.gameOwnerName) {
                    setOwnerName(gameCopy.gameOwnerName);
                } else {
                     console.warn("No game owner name found in game copy response for GameCopy ID:", gameCopyId, ". Using senderName prop as fallback.");
                     // Keep the ownerName state initialized with senderName prop
                }

                 // If gameId wasn't found, image state is already set, so return
                if (!foundGameId) return;

            } catch (err) {
                // Handle errors during fetch
                console.error('Failed to fetch game copy data for ID:', gameCopyId, err);
                setImageError(true); // General fetch failure also prevents image loading
                setImageLoading(false);
            }
        };

        fetchGameCopyData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [gameCopyId]); // Only re-run if gameCopyId changes

    // --- useEffect hook to fetch the image ---
    useEffect(() => {
        // Don't fetch if gameId is not set or if an error occurred earlier
        if (!gameId || imageError) {
            // If there was an error getting gameId, ensure loading is false
            if (!gameId) setImageLoading(false);
            return;
        }

        let objectUrl = null;

        const fetchImage = async () => {
            setImageLoading(true);
            setImageError(false); // Reset error state for this attempt
            setImageUrl(null);

            try {
                const response = await fetch(`http://localhost:8080/games/${gameId}/image`);
                if (!response.ok) {
                     if (response.status === 404) {
                        console.log(`Image not found for game ID ${gameId}. Using default.`);
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
                console.error(`Failed to fetch image blob for game ID ${gameId}:`, error);
                setImageError(true);
            } finally {
                setImageLoading(false);
            }
        };

        fetchImage();

        // Cleanup function
        return () => {
            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
            }
        };
    // Rerun only if gameId changes (and imageError is false initially)
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [gameId]);


    const toggleExpand = () => setExpanded(prev => !prev);

    // Calculate duration
    const start = startTime ? new Date(startTime) : null;
    const end = endTime ? new Date(endTime) : null;
    let duration = null;
    if (start && end && !isNaN(start.getTime()) && !isNaN(end.getTime())) {
        duration = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
    }

    // Format dates for display
    const formattedStartDate = formatDate(startTime);
    const formattedEndDate = formatDate(endTime);
    const headerDate = formattedStartDate || 'Date not specified';

    return (
        <div className={`event-card ${expanded ? 'expanded' : ''}`} onClick={toggleExpand} role="button" tabIndex="0" onKeyPress={(e) => (e.key === 'Enter' || e.key === ' ') && toggleExpand()} aria-expanded={expanded}>
            <div className="event-summary">
                <div className="event-header">
                    <div>
                        <h3>{gameName}</h3>
                        {/* Add Calendar Emoji to Header */}
                        <p>📅 {headerDate}</p>
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
                        alignItems: 'flex-start',
                        padding: '16px',
                        boxSizing: 'border-box',
                    }}
                >
                    {/* Image Column */}
                     <div style={{
                        width: '160px',
                        height: '160px',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        overflow: 'hidden',
                        borderRadius: '12px',
                        boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
                        backgroundColor: '#f0f0f0'
                    }}>
                        {imageLoading ? (
                            <CircularProgress size={32} />
                        ) : (
                            <img
                                src={imageError ? '/default-game-image.jpg' : imageUrl}
                                alt={`${gameName} cover`}
                                style={{
                                    display: 'block',
                                    width: '100%',
                                    height: '100%',
                                    objectFit: 'cover',
                                }}
                            />
                        )}
                    </div>

                    {/* Info Column */}
                    <div className="borrow-info">
                        {duration !== null && (
                             <div className="section">
                                <strong>Borrow Duration:</strong>
                                <p>{duration} day{duration !== 1 ? 's' : ''}</p>
                            </div>
                        )}
                        <div className="section">
                            {/* Add Calendar Emoji to Borrow Period Label */}
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
                             {/* Use ownerName state variable */}
                            <p>{ownerName}</p>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default BorrowedGameCard;