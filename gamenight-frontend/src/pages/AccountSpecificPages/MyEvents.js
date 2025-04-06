import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../../AuthContext';
import { usePopup } from "../../components/PopupContext";
import ClickableEventCard from '../../components/cards/ClickableEventCard';
import '../../styles/layout.css';

import '../../styles/button.css';

function MyEvents() {
    const { user } = useAuth();
    const { showPopup } = usePopup();
    const [events, setEvents] = useState([]);
    const [createdEvents, setCreatedEvents] = useState([]);
    const [playerId, setPlayerId] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showCreateEvent, setShowCreateEvent] = useState(false);
    const [newEventName, setNewEventName] = useState("");
    const [newEventDesc, setNewEventDesc] = useState("");
    const [newEventStart, setNewEventStart] = useState("");
    const [newEventEnd, setNewEventEnd] = useState("");
    const [allGames, setAllGames] = useState([]);
    const [selectedGameIds, setSelectedGameIds] = useState([]);

    useEffect(() => {
        const fetchAllGames = async () => {
            try {
                const response = await axios.get("/games");
                if (Array.isArray(response.data)) {
                    setAllGames(response.data);
                } else {
                     console.warn("Received non-array response when fetching all games:", response.data);
                     setAllGames([]);
                }
            } catch (e) {
                console.error("Failed to fetch all games:", e);
                showPopup("Could not load list of available games.", "error");
                setAllGames([]);
            }
        };
        fetchAllGames();
    }, [showPopup]);

    useEffect(() => {
        const fetchEvents = async () => {
            if (!user?.userId) {
                setIsLoading(false);
                setError("Please log in to view your events.");
                return;
            }
            setIsLoading(true);
            setError(null);
            setEvents([]);
            setCreatedEvents([]);

            try {
                const playerRes = await axios.get(`/users/${user.userId}/player-id`);
                const pid = playerRes.data;
                if (!pid) {
                   throw new Error("Could not retrieve player ID for the user.");
                }
                setPlayerId(pid);

                let createdList = [];
                try {
                    const createdRes = await axios.get(`/events/bycreator/${pid}`);
                    createdList = Array.isArray(createdRes.data) ? createdRes.data : [];
                    setCreatedEvents(createdList);
                } catch (e) {
                     console.error("Failed to load created events:", e);
                     showPopup("Warning: Could not load events created by you.", "warning");
                }

                const eventsRes = await axios.get(`/events/player/${pid}`);
                let registeredList = Array.isArray(eventsRes.data) ? eventsRes.data : [];

                const createdEventIds = new Set(createdList.map(ce => ce.id));
                const filteredRegisteredList = registeredList.filter(
                    (regEvent) => !createdEventIds.has(regEvent.id)
                );
                setEvents(filteredRegisteredList);

            } catch (err) {
                console.error("Error fetching events data:", err);
                setError(`Failed to load your events. ${err.message}`);
                setEvents([]);
                setCreatedEvents([]);
            } finally {
                setIsLoading(false);
            }
        };

        fetchEvents();
    }, [user?.userId, showPopup]);

     const sortEvents = (eventArray) => {
        return [...eventArray].sort((a, b) => {
            const now = new Date();
            const aExpired = a.endTime && new Date(a.endTime) < now;
            const bExpired = b.endTime && new Date(b.endTime) < now;
            if (!aExpired && bExpired) return -1;
            if (aExpired && !bExpired) return 1;
            if (!aExpired && !bExpired) {
                const startTimeA = a.startTime ? new Date(a.startTime).getTime() : Infinity;
                const startTimeB = b.startTime ? new Date(b.startTime).getTime() : Infinity;
                if (startTimeA !== startTimeB) return startTimeA - startTimeB;
                const endTimeA = a.endTime ? new Date(a.endTime).getTime() : Infinity;
                const endTimeB = b.endTime ? new Date(b.endTime).getTime() : Infinity;
                 return endTimeA - endTimeB;
            }
            if (aExpired && bExpired) {
                const endTimeA = a.endTime ? new Date(a.endTime).getTime() : -Infinity;
                const endTimeB = b.endTime ? new Date(b.endTime).getTime() : -Infinity;
                if (endTimeA !== endTimeB) return endTimeB - endTimeA;
                 const startTimeA = a.startTime ? new Date(a.startTime).getTime() : -Infinity;
                 const startTimeB = b.startTime ? new Date(b.startTime).getTime() : -Infinity;
                 return startTimeB - startTimeA;
            }
            return 0;
        });
    };
    const sortedEvents = sortEvents(events);
    const sortedCreatedEvents = sortEvents(createdEvents);

    const handleCreateEvent = async () => {
        if (!newEventName.trim()) {
            showPopup("Please provide an event name.", "warning");
            return;
        }
        if (!playerId) {
             showPopup("Cannot create event: Player ID not found.", "error");
             return;
        }
        const start = newEventStart ? new Date(newEventStart) : null;
        const end = newEventEnd ? new Date(newEventEnd) : null;
        if (start && end && start >= end) {
            showPopup("End time must be after start time.", "warning");
            return;
        }
        setIsLoading(true);
        try {
            const createRes = await axios.post("/events", {
                name: newEventName.trim(),
                description: newEventDesc.trim(),
                startTime: start ? start.toISOString() : null,
                endTime: end ? end.toISOString() : null,
                creatorId: playerId
            });
            const newEvent = createRes.data;
            if (!newEvent || !newEvent.id) {
                 throw new Error("Backend did not return a valid new event object.");
            }
            if (selectedGameIds.length > 0) {
                try {
                   await axios.post(`/events/${newEvent.id}/scheduleGames`, selectedGameIds);
                } catch (scheduleError) {
                     console.error("Error scheduling games for new event:", scheduleError);
                     showPopup("Event created, but failed to schedule selected games.", "warning");
                }
            }
            showPopup("Event created successfully!", "success");
            handleCancelCreate();
             window.location.reload(); // Or update state
        } catch (error) {
            console.error("Error creating event:", error);
            const errorMsg = error.response?.data?.message || error.message;
            showPopup(`Failed to create event: ${errorMsg}`, "error");
             setIsLoading(false); // Ensure loading stops on error
        }
        // setIsLoading(false); // Might conflict with reload
    };

    const handleCancelCreate = () => {
        setShowCreateEvent(false);
        setNewEventName("");
        setNewEventDesc("");
        setNewEventStart("");
        setNewEventEnd("");
        setSelectedGameIds([]);
    };

    const toggleSelectedGame = (gameId) => {
        setSelectedGameIds(prevSelected =>
            prevSelected.includes(gameId)
                ? prevSelected.filter(id => id !== gameId)
                : [...prevSelected, gameId]
        );
    };

    return (
        <div className="my-events-container" style={{ padding: '30px', position: 'relative', backgroundColor: '#fff' }}>
            {isLoading && <p className="centered loading-message">Loading your events...</p>}
            {error && <p className="centered error-message">{error}</p>}

            {!isLoading && !error && !showCreateEvent && user?.userId && (
                <div style={{ textAlign: 'right', marginBottom: '20px' }}>
                    <button
                        className="btn success create-event-btn"
                        onClick={() => setShowCreateEvent(true)}
                    >
                        + Create Event
                    </button>
                </div>
            )}

            {showCreateEvent && (
                 <div className="modal-backdrop">
                    <div className="modal-content">
                        <h2 className="modal-title">Create New Event</h2>
                        <div className="form-group">
                            <label className="form-label" htmlFor="eventName">Event Name:</label>
                            <input id="eventName" type="text" className="form-input" value={newEventName} onChange={(e) => setNewEventName(e.target.value)} placeholder="Enter event name..." required />
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="eventDesc">Description:</label>
                            <textarea id="eventDesc" className="form-input form-textarea" value={newEventDesc} onChange={(e) => setNewEventDesc(e.target.value)} placeholder="Describe your event..." />
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="eventStart">Start Time (Optional):</label>
                            <input id="eventStart" type="datetime-local" className="form-input" value={newEventStart} onChange={(e) => setNewEventStart(e.target.value)} />
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="eventEnd">End Time (Optional):</label>
                            <input id="eventEnd" type="datetime-local" className="form-input" value={newEventEnd} onChange={(e) => setNewEventEnd(e.target.value)} />
                        </div>
                         <div className="games-selection section">
                            <div className="games-selection-title">Select Games to Schedule (Optional):</div>
                             {allGames.length > 0 ? (
                                <div className="games-list-checkboxes">
                                    {allGames.map((game) => (
                                        <div key={game.id} className="game-option checkbox-option">
                                            <label>
                                                <input type="checkbox" className="game-checkbox" checked={selectedGameIds.includes(game.id)} onChange={() => toggleSelectedGame(game.id)} />
                                                <span className="checkbox-label">{game.name}</span>
                                            </label>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="empty">No games available to schedule.</p>
                            )}
                        </div>
                        <div className="modal-actions">
                            <button type="button" className="btn btn-secondary" onClick={handleCancelCreate}> Cancel </button>
                            <button type="button" className="btn btn-primary" onClick={handleCreateEvent} disabled={isLoading}> {isLoading ? 'Creating...' : 'Create Event'} </button>
                        </div>
                    </div>
                </div>
            )}

            {!isLoading && !error && !showCreateEvent && user?.userId && (
                <>
                    <h2 className="centered event-section-title"> Events You Created </h2>
                    {createdEvents.length > 0 ? (
                        <div className="event-list-container">
                            {sortedCreatedEvents.map((event) => (
                                <ClickableEventCard
                                    key={event.id}
                                    event={event}
                                    playerId={playerId}
                                    isCreator={true}
                                    showPopup={showPopup}
                                />
                            ))}
                        </div>
                    ) : ( <p className="centered empty-message">You haven't created any events yet.</p> )}

                    <hr className="divider" />

                    <h2 className="centered event-section-title"> Events You Are Registered For </h2>
                    {sortedEvents.length > 0 ? (
                        <div className="event-list-container">
                            {sortedEvents.map((event) => (
                                <ClickableEventCard
                                    key={event.id}
                                    event={event}
                                    playerId={playerId}
                                    isCreator={false}
                                    showPopup={showPopup}
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