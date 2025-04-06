import React, { useState, useEffect } from 'react';
import axios from 'axios';
import PropTypes from 'prop-types';
import '../../styles/EventCard.css';
import '../../styles/button.css'; 
import { usePopup } from '../../components/PopupContext'; 

const formatDateAndTime = (dateString) => {
    if (!dateString) return '';
    try {
        const d = new Date(dateString);
        if (isNaN(d.getTime())) {
             console.warn("[formatDateAndTime] Invalid date string received:", dateString);
             return 'Invalid Date';
        }
        const datePart = d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
        const timePart = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: true });
        return `${datePart} ${timePart}`;
    } catch (error) {
        console.error("[formatDateAndTime] Error formatting date:", dateString, error);
        return 'Invalid Date';
    }
};


function ClickableEventCard({ event, playerId, isCreator, showPopup: showInfoPopupProp, onEventCancelled, onUnregister }) { // Renamed prop slightly if needed
    const [isExpanded, setIsExpanded] = useState(false);
    const [scheduledGames, setScheduledGames] = useState([]);
    const [isLoadingGames, setIsLoadingGames] = useState(false);
    const [errorGames, setErrorGames] = useState(null);
    const [isCancelling, setIsCancelling] = useState(false);
    const [isUnregistering, setIsUnregistering] = useState(false);

    const { showPopup, setProcessing } = usePopup();

    const isExpired = event.endTime && new Date(event.endTime) < new Date();

    let dateLine = "";
    const startStr = event.startTime ? formatDateAndTime(event.startTime) : null;
    const endStr = event.endTime ? formatDateAndTime(event.endTime) : null;
    if (startStr && endStr) dateLine = `Starts: ${startStr} | Ends: ${endStr}`;
    else if (startStr) dateLine = `Starts: ${startStr}`;
    else if (endStr) dateLine = `Ends: ${endStr}`;
    else dateLine = "Date not specified";

    useEffect(() => {
        let isMounted = true;
        const fetchScheduledGames = async () => {
            if (!isMounted || !event.id) return;
            setIsLoadingGames(true); setErrorGames(null);
            try {
                const response = await axios.get(`/events/scheduledevent/${event.id}`);
                if (isMounted) setScheduledGames(Array.isArray(response.data) ? response.data : []);
            } catch (err) { console.error(`[EventCard ${event.id}] Error fetching scheduled games:`, err); if (isMounted) { setErrorGames("Could not load games."); setScheduledGames([]); }
            } finally { if (isMounted) setIsLoadingGames(false); }
        };
        if (isExpanded) { fetchScheduledGames(); }
        else { if (isMounted) { setScheduledGames([]); setErrorGames(null); setIsLoadingGames(false); } }
        return () => { isMounted = false; };
    }, [isExpanded, event.id]);


    // Toggle card expansion
    const toggleExpand = () => { setIsExpanded(!isExpanded); };


    const performCancelEvent = async () => {
        if (!isCreator || !event?.id || isCancelling) return;
        setIsCancelling(true); // Set local loading state

        try {
            await axios.delete(`/events/${event.id}`);
            if (onEventCancelled) onEventCancelled(event.id); // Call parent callback
        } catch (error) {
            console.error("Error canceling event:", error);
            showPopup(`Error canceling event: ${error.response?.data?.message || error.message}`); // Simple info popup for error
             setIsCancelling(false); // Reset local loading state on error
             setProcessing(false); // *** Ensure context processing state is reset on error ***
             throw error; // Re-throw to prevent context from auto-closing modal on failure
        }
    };

    const performUnregister = async () => {
        if (isCreator || !event?.id || !playerId || isUnregistering) return;
        setIsUnregistering(true); // Set local loading state

        try {
            await axios.delete(`/events/${event.id}/player/${playerId}`);
            if (onUnregister) onUnregister(event.id); // Call parent callback
        } catch (error) {
            console.error("Error unregistering from event:", error);
            showPopup(`Failed to unregister: ${error.response?.data?.message || error.message}`); // Simple info popup for error
            setIsUnregistering(false); // Reset local loading state on error
            setProcessing(false); // *** Ensure context processing state is reset on error ***
            throw error; // Re-throw to prevent context from auto-closing modal on failure
        }
    };


    // --- Handlers to SHOW Confirmation Popups using context ---

    const handleCancelEventClick = (e) => {
        e.stopPropagation();
        if (!isCreator || !event?.id || isCancelling) return;
        // Use the context's showPopup for confirmation
        showPopup({
            type: 'confirm',
            title: 'Confirm Cancellation',
            message: `Are you sure you want to permanently cancel the event "${event.name || 'Unnamed Event'}"?`,
            confirmText: 'Yes, Cancel Event',
            cancelText: 'No, Keep Event',
            confirmButtonVariant: 'danger', // This maps to confirmColor='error' in provider
            onConfirm: performCancelEvent // Pass the actual cancel function
        });
    };

    const handleUnregisterClick = (e) => {
        e.stopPropagation();
        if (isCreator || !event?.id || !playerId || isUnregistering) return;
        // Use the context's showPopup for confirmation
        showPopup({
            type: 'confirm',
            title: 'Confirm Unregistration',
            message: `Are you sure you want to unregister from the event "${event.name || 'Unnamed Event'}"?`,
            confirmText: 'Yes, Unregister',
            cancelText: 'No, Stay Registered',
            confirmButtonVariant: 'danger', // This maps to confirmColor='error'
            onConfirm: performUnregister // Pass the actual unregister function
        });
    };

    // --- Render JSX ---
    return (
        // No Fragment needed, modal is handled by context
        <div
            className={`event-card ${isExpired ? 'expired-event' : ''} ${isCreator ? 'creator-card' : 'registered-card'}`}
            onClick={toggleExpand}
            role="button" tabIndex="0"
            onKeyPress={(e) => (e.key === 'Enter' || e.key === ' ') && toggleExpand()}
            aria-expanded={isExpanded}
        >
            {/* Card Content... (Header, Description Summary, Details) */}
            {isExpired && <span className="expired-badge">Expired</span>}
            <div className="event-header"> <div className="event-summary"> <h3>{event.name || 'Unnamed Event'}</h3> <p className="event-date">📅 {dateLine}</p> </div> <span className={`chevron ${isExpanded ? 'expanded' : ''}`}>▼</span> </div>
            {!isExpanded && event.description && ( <p className="description-summary" style={{ marginTop: '0.5rem', fontSize: '0.9rem', color: '#555' }}> {event.description.length > 100 ? `${event.description.substring(0, 97)}...` : event.description} </p> )}
            {isExpanded && ( <div className="event-details"> {event.description && (<div className="section description-section"> <h4>Description</h4> <p className="description">{event.description}</p> </div>)} <div className="section games-section"> <h4>📋 Scheduled Games</h4> {isLoadingGames && <p className="loading">Loading games...</p>} {errorGames && <p className="error-message">{errorGames}</p>} {!isLoadingGames && !errorGames && ( scheduledGames.length > 0 ? (<ul className="games-list" style={{ listStyle: 'none', paddingLeft: 0 }}> {scheduledGames.map((game) => (<li key={game.id || game.name}>🎮 {game.name || 'Unnamed Game'}</li>))} </ul>) : (<p className="empty">No specific games listed.</p>) )} </div>

                {/* Action Buttons Container */}
                <div className="event-action-buttons" style={{ marginTop: '1rem', textAlign: 'right', display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                    {/* Cancel button - TRIGGERS CONTEXT POPUP */}
                    {isCreator && !isExpired && (
                        <button
                            // Apply smaller style class
                            className="btn btn-danger btn-cancel-event btn-card-action"
                            onClick={handleCancelEventClick}
                            disabled={isCancelling} // Disable button based on local state
                        >
                            Cancel My Event
                        </button>
                    )}
                    {/* Unregister button - TRIGGERS CONTEXT POPUP */}
                    {!isCreator && !isExpired && playerId && (
                        <button
                             // Apply smaller style class
                            className="btn btn-danger btn-unregister-event btn-card-action"
                            onClick={handleUnregisterClick}
                            disabled={isUnregistering} // Disable button based on local state
                        >
                            Unregister
                        </button>
                    )}
                </div>
            </div>
            )}
        </div>
        // No <ConfirmationModal> rendered here anymore
    );
}

// PropTypes
ClickableEventCard.propTypes = {
    event: PropTypes.shape({ id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired, name: PropTypes.string, description: PropTypes.string, startTime: PropTypes.string, endTime: PropTypes.string, }).isRequired,
    playerId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    isCreator: PropTypes.bool.isRequired,
    showPopup: PropTypes.func.isRequired, // Prop for showing simple info/error popups MAYBE (or just use context version?)
    onEventCancelled: PropTypes.func,
    onUnregister: PropTypes.func,
};

export default ClickableEventCard;