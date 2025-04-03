import React, { useState, useEffect, useContext } from "react";
import axios from "axios";
import { AuthContext } from "../AuthContext";
import '../App.css';
import { Box, Tabs, Tab } from "@mui/material";

function SentRequestsPage() {
  const [allRequests, setAllRequests] = useState([]);
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

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const filteredRequests =
    tabValue === 0
      ? allRequests.filter((req) => req.status === "Delivered")
      : allRequests.filter((req) =>
          req.status === "Accepted" || req.status === "Rejected"
        );

  return (
    <div>
      {/* Secondary Nav */}
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
          <Tab label="Sent BorrowingRequests" />
          <Tab label="Updated Status Requests" />
        </Tabs>
      </Box>

      {/* Container */}
      <div className="received-requests-container">
        <h2 className="left-align">
          {tabValue === 0 ? "Sent Borrowing Requests" : "Updated Status Requests"}
        </h2>

        {filteredRequests.length > 0 ? (
          filteredRequests.map((request, index) => (
            <div className="request-card" key={index}>
              <div className="request-header">
                <div className="request-user">
                  <div className="avatar">
                    {request.gameName.charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <div className="username">{request.gameName}</div>
                    <div className="request-badge">Sent</div>
                  </div>
                </div>
              </div>

              <div className="request-info">
                <p><strong>Status:</strong> {request.status}</p>
                <p><strong>Dates:</strong> {request.startTime} - {request.endTime}</p>
              </div>

            </div>
          ))
        ) : (
          <p>No requests found.</p>
        )}
      </div>
    </div>
  );
}

export default SentRequestsPage;
