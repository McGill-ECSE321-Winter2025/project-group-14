import React, { useState, useEffect, useContext } from "react";
import axios from "axios";
import "../App.css";
import RequestCard from "../components/RequestCard";
import { AuthContext } from "../AuthContext";
import {
  Box,
  Tabs,
  Tab,
  Typography,
} from "@mui/material";

function SentRequestsPage() {
  const [allRequests, setAllRequests] = useState([]);
  const [selectedRequest, setSelectedRequest] = useState(null);

  const { user } = useContext(AuthContext);
  const userId = user?.userId;

  const [tabValue, setTabValue] = useState(0);

  useEffect(() => {
    if (!user || !userId) return;
  
    const fetchSentRequests = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8080/borrowingRequests/${userId}/requests`,
          {
            headers: { "User-Id": userId },
          }
        );
        setAllRequests(response.data);
      } catch (error) {
        console.error("Error fetching sent requests:", error);
      }
    };
  
    fetchSentRequests();
  }, [user, userId]);
  

  const handleViewDetails = (request) => {
    setSelectedRequest(request);
  };

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
    setSelectedRequest(null); // clear details panel on tab change
  };

  const filteredRequests =
    tabValue === 0
      ? allRequests.filter((req) => req.status === "Delivered")
      : allRequests.filter((req) =>
          req.status === "Accepted" || req.status === "Rejected"
        );

  return (
    <div>
      {/* MUI Secondary NavBar */}
      <Box
        sx={{
          width: "100%",
          borderBottom: 1,
          borderColor: "divider",
          mb: 3,
          '& .MuiTabs-indicator': { backgroundColor: 'black', height: '3px' },
          '& .MuiTab-root': {
            color: '#666',
            fontSize: '1rem',
            textTransform: 'none',
            fontWeight: 500,
            padding: '12px 24px',
            minWidth: 'unset',
            '&.Mui-selected': { color: 'black', fontWeight: 600 },
            '&:hover': { color: 'black', opacity: 1 },
          },
        }}
      >
        <Tabs value={tabValue} onChange={handleTabChange} centered variant="fullWidth">
          <Tab label="Sent Borrowing Requests" />
          <Tab label="Updated Status Requests" />
        </Tabs>
      </Box>

      {/* Page Content */}
      <div className="container">
        <div className="left-column">
          <h1 className="left-align">
            {tabValue === 0 ? "Sent Borrowing Requests" : "Updated Status Requests"}
          </h1>
          <div className="card-list">
            {filteredRequests.length > 0 ? (
              filteredRequests.map((request, index) => (
                <RequestCard
                  key={index}
                  title={request.gameName}
                  status={request.status}
                  onViewDetails={() => handleViewDetails(request)}
                />
              ))
            ) : (
              <Typography>
                {tabValue === 0
                  ? "No delivered requests found."
                  : "No accepted or rejected requests found."}
              </Typography>
            )}
          </div>
        </div>

        <div className="divider"></div>

        <div className="right-column">
          {selectedRequest ? (
            <div className="details-box">
              <h2>{selectedRequest.gameName}</h2>
              <p>Status: {selectedRequest.status}</p>
              <p>Start Date: {selectedRequest.startTime}</p>
              <p>End Date: {selectedRequest.endTime}</p>
            </div>
          ) : (
            <div className="details-box">
              <p>Select a request to view details.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default SentRequestsPage;
