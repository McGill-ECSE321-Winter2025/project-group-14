import React, { useState, useEffect } from 'react';
import '../../styles/EventCard.css'; // Assuming EventCard styles are applicable/shared
import { CircularProgress } from '@mui/material';
import { GameHistoryAPI } from '../../GettingAllGamesAPI'; // Assuming this API exists

// Define the date formatting utility function (outside the component)
const formatDateAndTime = (dateString) => {
    if (!dateString) return ''; // Return empty string if input is null/undefined
    try {
        const d = new Date(dateString);
        // Check if the date is valid
        if (isNaN(d.getTime())) {
             console.warn("[formatDateAndTime] Invalid date string received:", dateString);
             return 'Invalid Date';
        }
        // Format date and time parts
        const datePart = d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
        const timePart = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true });
        return `${datePart} ${timePart}`; // Combine them
    } catch (error) {
        // Log error if formatting fails
        console.error("[formatDateAndTime] Error formatting date:", dateString, error);
        return 'Invalid Date'; // Return fallback string
    }
};


const BorrowedGameCard = ({ request }) => {
    const [expanded, setExpanded] = useState(false);
    const [imageUrl, setImageUrl] = useState(null);
    const [imageLoading, setImageLoading] = useState(true);
    const [imageError, setImageError] = useState(false);
    const [gameId, setGameId] = useState(null);

    // Destructure props from request
    const {
        // id: borrowingRequestId, // Uncomment if needed elsewhere
        gameCopyId,
        gameName = "Untitled Game",
        senderName = "Unknown", // Assuming this is the Owner's name in this context
        startTime, // Keep original string/date value from prop
        endTime    // Keep original string/date value from prop
    } = request;

    // --- useEffect hooks remain the same ---
    useEffect(() => {
        const fetchGameId = async () => {
             if (!gameCopyId) {
                console.error("GameCopy ID is missing, cannot fetch game ID.");
                setImageError(true);
                setImageLoading(false);
                return;
            }
            try {
                // Ensure GameHistoryAPI.getGameCopyById exists and works as expected
                const gameCopy = await GameHistoryAPI.getGameCopyById(gameCopyId);
                if (gameCopy?.game?.id) {
                    setGameId(gameCopy.game.id);
                } else {
                     console.error("No game found inside game copy response for GameCopy ID:", gameCopyId, gameCopy);
                     throw new Error("No game found inside game copy response");
                }
            } catch (err) {
                console.error("Failed to fetch game ID from game copy ID:", gameCopyId, err);
                setImageError(true);
                setImageLoading(false);
            }
        };

        fetchGameId();
    }, [gameCopyId]);

    useEffect(() => {
        if (!gameId) return; // Don't fetch if gameId is not set

        let objectUrl = null;

        const fetchImage = async () => {
            setImageLoading(true); // Ensure loading state is true at the start
            setImageError(false); // Reset error state
            setImageUrl(null); // Reset image URL

            try {
                const response = await fetch(`http://localhost:8080/games/${gameId}/image`);
                if (!response.ok) {
                     // Distinguish between truly not found and other errors
                     if (response.status === 404) {
                        console.log(`Image not found for game ID ${gameId}. Using default.`);
                        setImageError(true); // Use flag to indicate default image should be used
                     } else {
                         throw new Error(`HTTP error ${response.status}`);
                     }
                } else {
                    const imageBlob = await response.blob();
                    objectUrl = URL.createObjectURL(imageBlob);
                    setImageUrl(objectUrl);
                    setImageError(false); // Explicitly set error to false on success
                }

            } catch (error) {
                console.error(`Failed to fetch image blob for game ID ${gameId}:`, error);
                setImageError(true); // Use flag to indicate default image should be used
            } finally {
                setImageLoading(false);
            }
        };

        fetchImage();

        // Cleanup function to revoke the object URL
        return () => {
            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
            }
        };
    }, [gameId]); // Rerun when gameId changes


    const toggleExpand = () => setExpanded(prev => !prev);

    // Calculate duration (requires Date objects)
    // Keep these Date object creations if duration calculation is needed
    const start = startTime ? new Date(startTime) : null;
    const end = endTime ? new Date(endTime) : null;
    let duration = null;
    if (start && end && !isNaN(start.getTime()) && !isNaN(end.getTime())) {
        duration = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
    }

    // --- Use formatDateAndTime for display ---
    const formattedStartTime = formatDateAndTime(startTime);
    const formattedEndTime = formatDateAndTime(endTime);

    // Format for the header (using only start time/date)
    const headerDateTime = formattedStartTime || 'Date not specified'; // Use formatted start time or fallback

    return (
        <div className={`event-card ${expanded ? 'expanded' : ''}`} onClick={toggleExpand} role="button" tabIndex="0" onKeyPress={(e) => (e.key === 'Enter' || e.key === ' ') && toggleExpand()} aria-expanded={expanded}>
            <div className="event-summary">
                <div className="event-header">
                    <div>
                        <h3>{gameName}</h3>
                        {/* Display formatted start time/date in header */}
                        <p>{headerDateTime}</p>
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
                        alignItems: 'flex-start', // Align items to the top
                        padding: '16px',
                        boxSizing: 'border-box',
                    }}
                >
                    {/* Image Column */}
                     <div style={{
                        width: '160px', // Fixed width
                        height: '160px', // Fixed height for aspect ratio control
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        overflow: 'hidden', // Hide parts of image that don't fit container
                        borderRadius: '12px', // Apply border radius to container
                        boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
                        backgroundColor: '#f0f0f0' // Background for loading/error state
                    }}>
                        {imageLoading ? (
                            <CircularProgress size={32} />
                        ) : (
                            <img
                                src={imageError ? '/default-game-image.jpg' : imageUrl} // Use default image on error
                                alt={`${gameName} cover`}
                                style={{
                                    display: 'block', // Remove extra space below image
                                    width: '100%',
                                    height: '100%',
                                    objectFit: 'cover', // Cover ensures the container is filled
                                }}
                            />
                        )}
                    </div>


                    {/* Info Column */}
                    <div className="borrow-info">
                        {/* Show duration only if valid */}
                        {duration !== null && (
                             <div className="section">
                                <strong>Borrow Duration:</strong>
                                <p>{duration} day{duration !== 1 ? 's' : ''}</p>
                            </div>
                        )}
                         {/* Use formatDateAndTime for Borrow Period */}
                        <div className="section">
                        <strong> Borrow Period:</strong>
                             {formattedStartTime && formattedEndTime ? (
                                <p>📅 From {formattedStartTime} to {formattedEndTime}</p>
                            ) : formattedStartTime ? (
                                <p>Starts: {formattedStartTime}</p>
                            ) : formattedEndTime ? (
                                <p>Ends: {formattedEndTime}</p>
                            ) : (
                                <p>Date not specified</p>
                            )}
                        </div>
                        <div className="section">
                            <strong>Owner:</strong>
                            <p>{senderName}</p> {/* Assuming senderName is the owner */}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};


export default BorrowedGameCard;