import React, { useState, useEffect, useContext } from "react";
import axios from "axios";
import { AuthContext } from "../../AuthContext";
import '../../App.css'; 
import './SentRequestsPage.css';
import RequestCard from '../../components/cards/RequestCard';
import { Box, Tabs, Tab, CircularProgress } from "@mui/material"; 

function SentRequestsPage() {
  const [allRequests, setAllRequests] = useState([]);
  const { user } = useContext(AuthContext);
  const userId = user?.userId;

  const [tabValue, setTabValue] = useState(0);
  const [senderId, setSenderId] = useState(null);
  const [isLoading, setIsLoading] = useState(true); 
  const [error, setError] = useState(null); 

  useEffect(() => {
    if (!user || !userId) {
      setIsLoading(false);
      return;
    }
    setIsLoading(true); 
    setError(null); 
    const fetchSenderId = async () => { 
      try {
        const response = await axios.get(`http://localhost:8080/users/${userId}/player-id`, {
          headers: { "User-Id": userId } 
        });
        const fetchedSenderId = response.data;
        setSenderId(fetchedSenderId);
        if (fetchedSenderId) {
          fetchSentRequests(fetchedSenderId);
        } else {
           console.warn("Sender ID (Player ID) not found for user:", userId);
           setAllRequests([]); 
           setError("Could not find player details for this account."); 
           setIsLoading(false);
        }
      } catch (error) {
        console.error("Error fetching sender ID:", error);
        if (error.response && error.response.status === 404) {
             setError("Could not find player details for this account.");
        } else {
             setError("An error occurred while loading player details."); 
        }
        setAllRequests([]); 
        setIsLoading(false);
      }
    };

    const fetchSentRequests = async (currentSenderId) => {
      try {
        const requestsResponse = await axios.get(`http://localhost:8080/borrowingRequests/${currentSenderId}/sent-requests`, { 
          headers: { "User-Id": userId }
        });
        setAllRequests(requestsResponse.data || []); 
        setError(null); 
      } catch (error) {
        console.error("Error fetching sent requests:", error);
        setError("Could not load sent requests."); 
        setAllRequests([]); 
      } finally {
        setIsLoading(false); 
      }
    };
  
    fetchSenderId();
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

  const renderContent = () => {
    if (isLoading) {
      return <div className="message-area"><p>Loading requests...</p></div>; 
    }
    if (error) {
      return <div className="message-area"><p className="error-text">{error}</p></div>;
    }
    if (filteredRequests.length === 0) {
      return <div className="message-area"><p>No requests found.</p></div>;
    }

    return (
      <div className="requests-list-container">
        {filteredRequests.map((request) => (
          <RequestCard 
            key={request.id} 
            gameName={request.gameName} 
            status={request.status} 
            startTime={request.startTime} 
            endTime={request.endTime} 
          />
        ))}
      </div>
    );
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: 'calc(100vh - 64px)' }}> 
      <Box
        sx={{
          width: "100%",
          mb: 8, 
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
          flexShrink: 0 
        }}
      >
        <Tabs value={tabValue} onChange={handleTabChange} centered variant="fullWidth">
          <Tab label="Pending Approval Requests" />
          <Tab label="Answered Requests" />
        </Tabs>
      </Box>

      <div className="requests-content-area"> 
        <h2 className="page-title">
          {tabValue === 0 ? "Pending Approval Requests" : "Answered Requests"}
        </h2>
        {renderContent()}
      </div>
    </div>
  );
}

export default SentRequestsPage;