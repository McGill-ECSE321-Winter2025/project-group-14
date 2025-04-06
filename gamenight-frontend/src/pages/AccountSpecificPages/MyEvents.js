
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import axios from 'axios';
import { useAuth } from '../../AuthContext';
import { usePopup } from "../../components/PopupContext";
import ClickableEventCard from '../../components/cards/ClickableEventCard';
import '../../styles/layout.css';
import '../../styles/button.css';
import '../../styles/EventCard.css';
import './modal.css' 

function MyEvents() {
    const { user } = useAuth();
    const { showPopup } = usePopup();
    const [events, setEvents] = useState([]);
    const [createdEvents, setCreatedEvents] = useState([]);
    const [playerId, setPlayerId] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isCreating, setIsCreating] = useState(false);
    const [error, setError] = useState(null); 
    const [showCreateEvent, setShowCreateEvent] = useState(false);

    const [newEventName, setNewEventName] = useState("");
    const [newEventDesc, setNewEventDesc] = useState("");
    const [newEventStart, setNewEventStart] = useState("");
    const [newEventEnd, setNewEventEnd] = useState("");
    const [allGames, setAllGames] = useState([]); // List of games for scheduling
    const [selectedGameIds, setSelectedGameIds] = useState([]); // IDs of games selected in form
    const [isFetchingGames, setIsFetchingGames] = useState(false); // Added state for loading games list

    const fetchAllGames = useCallback(async () => {
        setIsFetchingGames(true); // Set loading true
        try {
            console.log("[MyEvents] Fetching all games for create form...");
            const response = await axios.get("/games");
            setAllGames(Array.isArray(response.data) ? response.data : []);
            console.log("[MyEvents] Fetched games:", response.data?.length);
        } catch (e) {
            console.error("[MyEvents] Failed to fetch all games:", e);
            showPopup("Could not load list of available games.", "error");
            setAllGames([]);
        } finally {
             setIsFetchingGames(false); // Set loading false
        }
    }, [showPopup]); // Dependencies

    useEffect(() => {
        if (showCreateEvent) {
            fetchAllGames();
        } else {
             setAllGames([]); // Clear game list when modal closes
        }
    }, [showCreateEvent, fetchAllGames]); // Rerun when modal opens/closes


    // --- Fetch User's Player ID and Associated Events ---
    const fetchEventsData = useCallback(async () => {
        if (!user?.userId) { setIsLoading(false); setError("Please log in."); setEvents([]); setCreatedEvents([]); setPlayerId(null); return; }
        setIsLoading(true); setError(null);
        let pid;
        try {
            const playerRes = await axios.get(`/users/${user.userId}/player-id`);
            pid = playerRes.data; if (!pid) throw new Error("Could not retrieve player ID."); setPlayerId(pid);
            let createdList = [];
            try { const createdRes = await axios.get(`/events/bycreator/${pid}`); createdList = Array.isArray(createdRes.data) ? createdRes.data : []; setCreatedEvents(createdList); } catch (e) { console.error("Failed to load created events:", e); showPopup("Warning: Could not load created events.", "warning"); setCreatedEvents([]); }
            const eventsRes = await axios.get(`/events/player/${pid}`); let registeredList = Array.isArray(eventsRes.data) ? eventsRes.data : [];
            const createdEventIds = new Set(createdList.map(ce => ce.id)); const filteredRegisteredList = registeredList.filter( (regEvent) => !createdEventIds.has(regEvent.id) ); setEvents(filteredRegisteredList);
        } catch (err) { console.error("Error fetching events data:", err); setError(`Failed to load events: ${err.message}`); setEvents([]); setCreatedEvents([]); if (!pid) setPlayerId(null);
        } finally { setIsLoading(false); }
    }, [user?.userId, showPopup]);

    useEffect(() => { fetchEventsData(); }, [fetchEventsData]);


     const sortEvents = (eventArray) => { /* ... same sorting logic ... */ return [...eventArray].sort((a, b)=>{ const now = new Date(); const aE = a.endTime && new Date(a.endTime) < now; const bE = b.endTime && new Date(b.endTime) < now; if (!aE && bE) return -1; if (aE && !bE) return 1; if (!aE && !bE) { const sA = a.startTime ? new Date(a.startTime).getTime() : Infinity; const sB = b.startTime ? new Date(b.startTime).getTime() : Infinity; if (sA !== sB) return sA - sB; const eA = a.endTime ? new Date(a.endTime).getTime() : Infinity; const eB = b.endTime ? new Date(b.endTime).getTime() : Infinity; return eA - eB; } if (aE && bE) { const eA = a.endTime ? new Date(a.endTime).getTime() : -Infinity; const eB = b.endTime ? new Date(b.endTime).getTime() : -Infinity; if (eA !== eB) return eB - eA; const sA = a.startTime ? new Date(a.startTime).getTime() : -Infinity; const sB = b.startTime ? new Date(b.startTime).getTime() : -Infinity; return sB - sA; } return 0; }); };
    const sortedCreatedEvents = useMemo(() => sortEvents(createdEvents), [createdEvents]);
    const sortedRegisteredEvents = useMemo(() => sortEvents(events), [events]);


    const handleCreateEvent = async () => {
        
        if (!newEventName.trim()) { showPopup("Please provide an event name.", "warning"); return; }
        if (!playerId) { showPopup("Cannot create event: Player ID not found.", "error"); return; }
        const start = newEventStart ? new Date(newEventStart) : null;
        const end = newEventEnd ? new Date(newEventEnd) : null;
        if (start && end && start >= end) { showPopup("End time must be after start time.", "warning"); return; }

        setIsCreating(true);
        let newEvent;
        try {
            const eventCreationPayload = { name: newEventName.trim(), description: newEventDesc.trim(), startTime: start?.toISOString(), endTime: end?.toISOString() };
            const createRes = await axios.post("/events", eventCreationPayload);
            newEvent = createRes.data;
            if (!newEvent?.id) throw new Error("Backend did not return valid event data.");

            if (selectedGameIds.length > 0) { try { await axios.post(`/events/${newEvent.id}/scheduleGames`, selectedGameIds); } catch (err) { showPopup(`Event created, but failed to schedule games.`, "warning"); }}

            const registrationUrl = `/events/${newEvent.id}/player/${playerId}`;
            try { await axios.post(registrationUrl); }
            catch (err) { showPopup(`EVENT CREATED, BUT AUTO-REGISTRATION FAILED: ${err.response?.data?.message || err.message}.`, "error"); }

            setCreatedEvents(prev => sortEvents([newEvent, ...prev])); // Add and re-sort immediately
            handleCancelCreate(); // Close modal

        } catch (error) {
             console.error("[handleCreateEvent] Error creating event:", error);
             showPopup(`Failed to create event: ${error.response?.data?.message || error.message}`, "error");
        } finally {
            setIsCreating(false);
        }
    };


    const handleCancelCreate = () => {
        setShowCreateEvent(false);
        setNewEventName(""); setNewEventDesc(""); setNewEventStart(""); setNewEventEnd("");
        setSelectedGameIds([]); setAllGames([]);
    };

    const toggleSelectedGame = (gameId) => {
        setSelectedGameIds(prevSelected =>
            prevSelected.includes(gameId)
                ? prevSelected.filter(id => id !== gameId)
                : [...prevSelected, gameId]
        );
    };

    const handleEventCancelled = useCallback((cancelledEventId) => {
        setCreatedEvents(prev => prev.filter(event => event.id !== cancelledEventId));
        showPopup("Event cancelled.", "success");
    }, [showPopup]);
    const handleUnregisterFromEvent = useCallback((unregisteredEventId) => {
        setEvents(prevEvents => prevEvents.filter(event => event.id !== unregisteredEventId));
        showPopup("Successfully unregistered.", "success");
    }, [showPopup]);

    // --- Render Logic ---
    return (
        <div className="my-events-container" style={{ padding: '30px', position: 'relative', backgroundColor: '#fff' }}>
            {/* Loading / Error State */}
            {isLoading && <p className="centered loading-message">Loading your events...</p>}
            {error && <p className="centered error-message">{error}</p>}

            {/* Create Event Button */}
            {/* Position using CSS on .create-event-btn if needed, or keep wrapper */}
            {!isLoading && !error && !showCreateEvent && user?.userId && (
                <div style={{ textAlign: 'right', marginBottom: '20px' }}>
                     {/* Ensure class="btn btn-success create-event-btn" matches your button.css */}
                    <button
                        className="btn btn-success create-event-btn"
                        onClick={() => setShowCreateEvent(true)}
                        disabled={isCreating} // Disable button while create modal is submitting
                    >
                        + Create Event
                    </button>
                </div>
            )}

            {/* Create Event Modal Popup */}
            {showCreateEvent && (
                 // Use CSS classes for modal structure
                 <div className="modal-backdrop" onClick={handleCancelCreate}>
                    <div className="modal-content" onClick={e => e.stopPropagation()}>
                        <h2 className="modal-title">Create New Event</h2>
                        {/* Form submission handled by button click calling handleCreateEvent */}
                        <form onSubmit={(e) => { e.preventDefault(); handleCreateEvent(); }}>
                            {/* Event Name */}
                            <div className="form-group">
                                <label className="form-label" htmlFor="eventName">Event Name <span style={{color: 'red'}}>*</span></label>
                                <input id="eventName" type="text" className="form-input" value={newEventName} onChange={(e) => setNewEventName(e.target.value)} placeholder="Enter event name..." required />
                            </div>
                            {/* Description */}
                            <div className="form-group">
                                <label className="form-label" htmlFor="eventDesc">Description:</label>
                                <textarea id="eventDesc" className="form-input form-textarea" value={newEventDesc} onChange={(e) => setNewEventDesc(e.target.value)} placeholder="Describe your event..." />
                            </div>
                            {/* Start Time */}
                            <div className="form-group">
                                <label className="form-label" htmlFor="eventStart">Start Time (Optional):</label>
                                <input id="eventStart" type="datetime-local" className="form-input" value={newEventStart} onChange={(e) => setNewEventStart(e.target.value)} />
                            </div>
                            {/* End Time */}
                            <div className="form-group">
                                <label className="form-label" htmlFor="eventEnd">End Time (Optional):</label>
                                <input id="eventEnd" type="datetime-local" className="form-input" value={newEventEnd} onChange={(e) => setNewEventEnd(e.target.value)} />
                            </div>
                             {/* Game Selection */}
                             <div className="games-selection"> {/* Use .games-selection */}
                                <div className="games-selection-title">Select Games to Schedule (Optional):</div>
                                {isFetchingGames ? (
                                    <p className="loading-message" style={{fontSize: '0.9rem', margin: '1rem 0'}}>Loading games list...</p>
                                ) : allGames.length > 0 ? (
                                    // Use .games-list class for the scrollable container
                                    <div className="games-list">
                                        {allGames.map((game) => (
                                            // Use .game-option class for each item
                                            <div key={game.id} className="game-option">
                                                {/* Use label around input+text */}
                                                <label style={{ display: 'inline-flex', alignItems: 'center', cursor: 'pointer' }}>
                                                    <input
                                                        type="checkbox"
                                                        className="game-checkbox" // Use .game-checkbox
                                                        checked={selectedGameIds.includes(game.id)}
                                                        onChange={() => toggleSelectedGame(game.id)}
                                                     />
                                                     {/* Use .checkbox-label class if defined in CSS */}
                                                    <span className="checkbox-label" style={{ marginLeft: '8px' }}>{game.name}</span>
                                                </label>
                                            </div>
                                        ))}
                                    </div>
                                ) : ( <p className="empty-message" style={{fontSize: '0.9rem'}}>No games available to schedule.</p> )}
                            </div>
                            {/* Modal Actions */}
                            <div className="modal-actions"> {/* Use .modal-actions */}
                                {/* Standard HTML buttons using your .btn classes */}
                                <button type="button" className="btn btn-secondary" onClick={handleCancelCreate} disabled={isCreating}> Cancel </button>
                                <button type="submit" className="btn btn-primary" disabled={isCreating || !newEventName.trim()}> {isCreating ? 'Creating...' : 'Create Event'} </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Display Event Lists */}
            {!isLoading && !error && !showCreateEvent && user?.userId && (
                <>
                    {/* Events You Created Section */}
                    <h2 className="centered event-section-title"> Events You Created </h2>
                    {sortedCreatedEvents.length > 0 ? (
                        <div className="event-list-container">
                            {sortedCreatedEvents.map((event) => (
                                <ClickableEventCard
                                    key={event.id}
                                    event={event}
                                    playerId={playerId}
                                    isCreator={true}
                                    showPopup={showPopup} // Pass for errors in card
                                    onEventCancelled={handleEventCancelled}
                                />
                            ))}
                        </div>
                    ) : ( <p className="centered empty-message">You haven't created any events yet.</p> )}

                    <hr className="divider" />

                    {/* Events You Are Registered For Section */}
                    <h2 className="centered event-section-title"> Events You Are Registered For </h2>
                    {sortedRegisteredEvents.length > 0 ? (
                        <div className="event-list-container">
                            {sortedRegisteredEvents.map((event) => (
                                <ClickableEventCard
                                    key={event.id}
                                    event={event}
                                    playerId={playerId}
                                    isCreator={false}
                                    showPopup={showPopup} // Pass for errors in card
                                    onUnregister={handleUnregisterFromEvent}
                                />
                            ))}
                        </div>
                    ) : ( <p className="centered empty-message">You are not registered for any other events.</p> )}
                </>
            )}
        </div>
    );
}

export default MyEvents;