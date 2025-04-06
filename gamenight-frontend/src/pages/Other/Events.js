import React, { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../AuthContext";
import { usePopup } from "../../components/PopupContext";
import "../../styles/events.css";
import "../../styles/button.css";

function Events() {
  const { user } = useAuth();
  const { showPopup } = usePopup();

  const [playerId, setPlayerId] = useState(null);
  const [allEvents, setAllEvents] = useState([]);
  const [createdByMe, setCreatedByMe] = useState([]);
  const [loading, setLoading] = useState(true);

  const [searchTerm, setSearchTerm] = useState("");
  const [filterStart, setFilterStart] = useState("");
  const [filterEnd, setFilterEnd] = useState("");
  const [showFilter, setShowFilter] = useState(false);

  const [sortField, setSortField] = useState("start");
  const [sortOrder, setSortOrder] = useState("asc");
  const [showSort, setShowSort] = useState(false);

  const navigate = useNavigate();

  const fetchAllEvents = useCallback(async () => {
    try {
      const response = await fetch("http://localhost:8080/events");
      if (!response.ok) {
        throw new Error("Failed to fetch events");
      }
      const data = await response.json();
      setAllEvents(data);
    } catch (error) {
      console.error("Error fetching events:", error);
      showPopup("Error fetching events: " + error.message);
      setAllEvents([]);
    }
  }, [showPopup]);

  const fetchPlayerIdAndCreatedEvents = useCallback(async () => {
    if (!user?.userId) {
      setLoading(false);
      return;
    }
    try {
      const res = await fetch(`http://localhost:8080/users/${user.userId}/player-id`);
      if (!res.ok) {
        throw new Error("Failed to fetch player ID");
      }
      const pid = await res.json();
      setPlayerId(pid);

      const createdRes = await fetch(`http://localhost:8080/events/bycreator/${pid}`);
      if (createdRes.ok) {
        const createdData = await createdRes.json();
        setCreatedByMe(createdData);
      }
    } catch (err) {
      console.error("Error fetching events created by user:", err);
      showPopup("Error fetching events created by user: " + err.message);
    } finally {
      setLoading(false);
    }
  }, [user, showPopup]);

  useEffect(() => {
    setLoading(true);
    Promise.all([fetchAllEvents(), fetchPlayerIdAndCreatedEvents()])
      .then(() => setLoading(false))
      .catch(() => setLoading(false));
  }, [fetchAllEvents, fetchPlayerIdAndCreatedEvents]);

  // Exclude events created by the user.
  const events = allEvents.filter(
    (evt) => !createdByMe.some((myEvt) => myEvt.id === evt.id)
  );

  let filteredEvents = events;
  if (searchTerm.trim() !== "") {
    const lowerTerm = searchTerm.toLowerCase();
    filteredEvents = filteredEvents.filter(
      (event) =>
        event.name.toLowerCase().includes(lowerTerm) ||
        event.description.toLowerCase().includes(lowerTerm)
    );
  }

  if (filterStart) {
    const startFilter = new Date(filterStart);
    filteredEvents = filteredEvents.filter(
      (event) => new Date(event.startTime) >= startFilter
    );
  }
  if (filterEnd) {
    const endFilter = new Date(filterEnd);
    filteredEvents = filteredEvents.filter(
      (event) => new Date(event.endTime) <= endFilter
    );
  }

  const sortedEvents = [...filteredEvents].sort((a, b) => {
    let result = 0;
    if (sortField === "name") {
      result = a.name.localeCompare(b.name, undefined, { sensitivity: "base" });
    } else {
      const dateA = new Date(a.startTime).getTime();
      const dateB = new Date(b.startTime).getTime();
      result = dateA - dateB;
    }
    return sortOrder === "asc" ? result : -result;
  });

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
      </div>
    );
  }

  // EventCard component using plain HTML
  const EventCard = ({ event }) => {
    const navigate = useNavigate();
    const [games, setGames] = useState([]);

    useEffect(() => {
      async function fetchGames() {
        try {
          const response = await fetch(
            `http://localhost:8080/events/scheduledevent/${event.id}`
          );
          if (!response.ok) {
            throw new Error("Failed to fetch games for event");
          }
          const data = await response.json();
          setGames(data);
        } catch (error) {
          console.error("Error fetching games:", error);
          showPopup("Error fetching games: " + error.message);
        }
      }
      fetchGames();
    }, [event.id, showPopup]);

    const hasValidStartTime = event.startTime && new Date(event.startTime).getTime() > 0;
    const hasValidEndTime = event.endTime && new Date(event.endTime).getTime() > 0;

    const formattedStart = hasValidStartTime
      ? new Date(event.startTime).toLocaleString("en-US", {
          month: "numeric",
          day: "numeric",
          year: "numeric",
          hour: "numeric",
          minute: "numeric",
          hour12: true,
        })
      : null;

    const formattedEnd = hasValidEndTime
      ? new Date(event.endTime).toLocaleString("en-US", {
          month: "numeric",
          day: "numeric",
          year: "numeric",
          hour: "numeric",
          minute: "numeric",
          hour12: true,
        })
      : null;

    return (
      <div className="card event-card">
        <div className="card-content">
          <h3 className="event-title">{event.name}</h3>
          <div className="event-date-box">
            {hasValidStartTime && (
              <p className="event-date">
                 Start: {formattedStart}
              </p>
            )}
            {hasValidEndTime && (
              <p className="event-date">
                 End: {formattedEnd}
              </p>
            )}
          </div>
          <p className="event-description">{event.description}</p>
          {games.length > 0 ? (
            <div>
              <h4>Games Scheduled:</h4>
              <ul>
                {games.map((g) => (
                  <li key={g.id}>{g.name}</li>
                ))}
              </ul>
            </div>
          ) : (
            <p>No games scheduled.</p>
          )}
          <div className="card-actions">
            <button
              className="view-details-btn"
              onClick={() => navigate(`/events/${event.id}`)}
            >
              View Details
            </button>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="events-container">
      <div className="events-header">
        <h4 className="page-title">Explore Events</h4>
        <div className="search-controls">
          <input
            type="text"
            placeholder="Search"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-field"
          />
          <button onClick={() => setShowFilter(!showFilter)} className="filter-btn">
            Filter
          </button>
          <button onClick={() => setShowSort(!showSort)} className="sort-btn">
            Sort
          </button>
        </div>
      </div>

      {showFilter && (
        <div className="popover filter-popover">
          <div className="filter-popover-content">
            <h4 className="popover-title">Filter Events</h4>
            <label>
              Start Date (≥)
              <input
                type="date"
                value={filterStart}
                onChange={(e) => setFilterStart(e.target.value)}
                className="date-filter"
              />
            </label>
            <label>
              End Date (≤)
              <input
                type="date"
                value={filterEnd}
                onChange={(e) => setFilterEnd(e.target.value)}
                className="date-filter"
              />
            </label>
            <div className="filter-actions">
              <button
                onClick={() => {
                  setFilterStart("");
                  setFilterEnd("");
                  setShowFilter(false);
                }}
                className="clear-btn"
              >
                Clear
              </button>
              <button onClick={() => setShowFilter(false)} className="apply-btn">
                Apply
              </button>
            </div>
          </div>
        </div>
      )}

      {showSort && (
        <div className="popover sort-popover">
          <div className="sort-popover-content">
            <h4 className="popover-title">Sort Events</h4>
            <label>
              Field
              <select
                value={sortField}
                onChange={(e) => setSortField(e.target.value)}
                className="sort-control"
              >
                <option value="name">Alphabetical (Name)</option>
                <option value="start">Start Date</option>
              </select>
            </label>
            <label>
              Order
              <select
                value={sortOrder}
                onChange={(e) => setSortOrder(e.target.value)}
                className="sort-control"
              >
                <option value="asc">Ascending</option>
                <option value="desc">Descending</option>
              </select>
            </label>
            <div className="sort-actions">
              <button onClick={() => setShowSort(false)} className="apply-btn">
                Apply
              </button>
            </div>
          </div>
        </div>
      )}

      {sortedEvents.filter((evt) => !(evt.endTime && new Date(evt.endTime) < new Date())).length === 0 ? (
        <p className="no-events-message">No events available.</p>
      ) : (
        <div className="events-grid">
          {sortedEvents
            .filter((evt) => !(evt.endTime && new Date(evt.endTime) < new Date()))
            .map((event) => (
              <EventCard key={event.id} event={event} />
            ))}
        </div>
      )}
    </div>
  );
}

export default Events;
