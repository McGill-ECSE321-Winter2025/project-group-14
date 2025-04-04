
import React, { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  CircularProgress,
  Grid,
  Card,
  CardContent,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Popover,
} from "@mui/material";
import { useAuth } from "../../AuthContext"; 

function Events() {
  const { user } = useAuth(); 

  const [playerId, setPlayerId] = useState(null);
  const [allEvents, setAllEvents] = useState([]);   
  const [createdByMe, setCreatedByMe] = useState([]); 
  const [loading, setLoading] = useState(true);

 
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStart, setFilterStart] = useState("");
  const [filterEnd, setFilterEnd] = useState("");
  const [filterAnchorEl, setFilterAnchorEl] = useState(null);

  const [sortField, setSortField] = useState("start");
  const [sortOrder, setSortOrder] = useState("asc");
  const [sortAnchorEl, setSortAnchorEl] = useState(null);

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
      setAllEvents([]);
    }
  }, []);

  
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
    } finally {
      setLoading(false);
    }
  }, [user]);

  
  useEffect(() => {
    setLoading(true);
    Promise.all([fetchAllEvents(), fetchPlayerIdAndCreatedEvents()])
      .then(() => setLoading(false))
      .catch(() => setLoading(false));
  }, [fetchAllEvents, fetchPlayerIdAndCreatedEvents]);

  
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

  
  const handleFilterClick = (event) => {
    setFilterAnchorEl(event.currentTarget);
  };
  const handleFilterClose = () => {
    setFilterAnchorEl(null);
  };
  const handleApplyFilters = () => {
    handleFilterClose();
  };
  const handleClearFilters = () => {
    setFilterStart("");
    setFilterEnd("");
    handleFilterClose();
  };
  const filterOpen = Boolean(filterAnchorEl);
  const filterPopoverId = filterOpen ? "filter-popover" : undefined;

  const handleSortClick = (event) => {
    setSortAnchorEl(event.currentTarget);
  };
  const handleSortClose = () => {
    setSortAnchorEl(null);
  };
  const handleApplySort = () => {
    handleSortClose();
  };
  const sortOpen = Boolean(sortAnchorEl);
  const sortPopoverId = sortOpen ? "sort-popover" : undefined;

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  
  const EventCard = ({ event }) => {
    const navigate = useNavigate();
    const [games, setGames] = useState([]);

    useEffect(() => {
      async function fetchGames() {
        try {
          const response = await fetch(`http://localhost:8080/events/scheduledevent/${event.id}`);
          if (!response.ok) {
            throw new Error("Failed to fetch games for event");
          }
          const data = await response.json();
          setGames(data);
        } catch (error) {
          console.error("Error fetching games:", error);
        }
      }
      fetchGames();
    }, [event.id]);

    const formattedStart = new Date(event.startTime).toLocaleString("en-US", {
      month: "numeric",
      day: "numeric",
      year: "numeric",
      hour: "numeric",
      minute: "numeric",
      hour12: true,
    });
    const formattedEnd = new Date(event.endTime).toLocaleString("en-US", {
      month: "numeric",
      day: "numeric",
      year: "numeric",
      hour: "numeric",
      minute: "numeric",
      hour12: true,
    });

    return (
      <Grid item xs={12} sm={6} md={4} key={event.id}>
        <Card>
          <CardContent>
            <Typography variant="h6">{event.name}</Typography>
            {games.length > 0 ? (
              <Box mb={1}>
                <Typography variant="subtitle2">Games Scheduled:</Typography>
                <ul style={{ margin: 0, paddingLeft: "20px" }}>
                  {games.map((g) => (
                    <li key={g.id}>{g.name}</li>
                  ))}
                </ul>
              </Box>
            ) : (
              <Box mb={1}>
                <Typography variant="subtitle2">No games scheduled.</Typography>
              </Box>
            )}
            <Typography variant="body2">{event.description}</Typography>
            <Typography variant="body2">Start: {formattedStart}</Typography>
            <Typography variant="body2">End: {formattedEnd}</Typography>
            <Box mt={2}>
              <Button
                variant="contained"
                color="primary"
                onClick={() => navigate(`/events/${event.id}`)}
              >
                View Details
              </Button>
            </Box>
          </CardContent>
        </Card>
      </Grid>
    );
  };

  return (
    <div className="container">
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h4">Events</Typography>
        <Box display="flex" gap={2} alignItems="center">
          <TextField
            label="Search"
            variant="outlined"
            size="small"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <Button variant="outlined" onClick={handleFilterClick}>
            Filter
          </Button>
          <Button variant="outlined" onClick={handleSortClick}>
            Sort
          </Button>
        </Box>
      </Box>

      {}
      <Popover
        id={filterPopoverId}
        open={filterOpen}
        anchorEl={filterAnchorEl}
        onClose={handleFilterClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
        transformOrigin={{ vertical: "top", horizontal: "right" }}
      >
        <Box p={2} display="flex" flexDirection="column" gap={2} minWidth={250}>
          <Typography variant="subtitle1">Filter Events</Typography>
          <TextField
            label="Start Date (≥)"
            type="date"
            InputLabelProps={{ shrink: true }}
            value={filterStart}
            onChange={(e) => setFilterStart(e.target.value)}
          />
          <TextField
            label="End Date (≤)"
            type="date"
            InputLabelProps={{ shrink: true }}
            value={filterEnd}
            onChange={(e) => setFilterEnd(e.target.value)}
          />
          <Box display="flex" justifyContent="flex-end" gap={1}>
            <Button variant="outlined" size="small" onClick={handleClearFilters}>
              Clear
            </Button>
            <Button variant="contained" size="small" onClick={handleApplyFilters}>
              Apply
            </Button>
          </Box>
        </Box>
      </Popover>

      {}
      <Popover
        id={sortPopoverId}
        open={sortOpen}
        anchorEl={sortAnchorEl}
        onClose={handleSortClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
        transformOrigin={{ vertical: "top", horizontal: "right" }}
      >
        <Box p={2} display="flex" flexDirection="column" gap={2} minWidth={250}>
          <Typography variant="subtitle1">Sort Events</Typography>
          <FormControl variant="outlined" size="small">
            <InputLabel id="sort-field-label">Field</InputLabel>
            <Select
              labelId="sort-field-label"
              value={sortField}
              label="Field"
              onChange={(e) => setSortField(e.target.value)}
            >
              <MenuItem value="name">Alphabetical (Name)</MenuItem>
              <MenuItem value="start">Start Date</MenuItem>
            </Select>
          </FormControl>
          <FormControl variant="outlined" size="small">
            <InputLabel id="sort-order-label">Order</InputLabel>
            <Select
              labelId="sort-order-label"
              value={sortOrder}
              label="Order"
              onChange={(e) => setSortOrder(e.target.value)}
            >
              <MenuItem value="asc">Ascending</MenuItem>
              <MenuItem value="desc">Descending</MenuItem>
            </Select>
          </FormControl>
          <Box display="flex" justifyContent="flex-end" gap={1}>
            <Button variant="contained" size="small" onClick={handleApplySort}>
              Apply
            </Button>
          </Box>
        </Box>
      </Popover>

      {sortedEvents.length === 0 ? (
        <Typography>No events available.</Typography>
      ) : (
        <Grid container spacing={3}>
          {sortedEvents.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </Grid>
      )}
    </div>
  );
}

export default Events;
