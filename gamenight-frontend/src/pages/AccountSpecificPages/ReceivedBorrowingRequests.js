import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import ReceivedRequestCard from '../../components/cards/ReceivedRequestCard';
import { AuthContext } from '../../AuthContext';
import '../../styles/layout.css'; 
import { CircularProgress } from "@mui/material"; 

const ReceivedBorrowingRequests = () => {
  const { user, loading: authLoading, isOwner } = useContext(AuthContext); 
  const navigate = useNavigate();
  const [requests, setRequests] = useState([]);
  const [error, setError] = useState(null);
  const [accessDenied, setAccessDenied] = useState(false);
  const [isLoading, setIsLoading] = useState(true); 

  useEffect(() => {
     if (authLoading) {
        setIsLoading(true); 
        return; 
    }
    if (!user) {
        setError("Please log in to view requests.");
        setIsLoading(false); 
        setAccessDenied(false); 
        return;
    }
    if (!isOwner) {
      setAccessDenied(true);
      setError(null); 
      setIsLoading(false); 
      return;
    }
    setIsLoading(true); 
    setAccessDenied(false);
    setError(null);
    const token = localStorage.getItem('token'); 
    const fetchOwnerIdAndRequests = async () => {
      let fetchedOwnerId;
      try {
        const ownerIdResponse = await axios.get(
          `http://localhost:8080/users/${user.userId}/owner-id`, { headers: { 'User-Id': user.userId.toString() } }
        );
        fetchedOwnerId = ownerIdResponse.data;
        if (!fetchedOwnerId) throw new Error("Could not retrieve owner ID for this user."); 

        const response = await axios.get(
          `http://localhost:8080/borrowingRequests/owners/${fetchedOwnerId}/lending-history`, { headers: { 'User-Id': user.userId.toString() } }
        );
        const pendingRequests = (response.data || []).filter(request => request.status === 'Delivered' || request.status === 0);
        setRequests(pendingRequests);
      } catch (err) {
        console.error('Error fetching received requests data:', err);
        setError(err.response?.data?.message || err.message || "Failed to load requests.");
        setRequests([]); 
      } finally { setIsLoading(false); }
    };
    fetchOwnerIdAndRequests();
  }, [authLoading, user, isOwner]); 

  const handleAccept = (id) => {
    if (!user || !user.userId) return;
    axios.put(`http://localhost:8080/borrowingRequests/${id}/status`, null, {
        params: { status: 'Accepted', action: 'respond' }, headers: { 'User-Id': user.userId.toString() }
      })
      .then(response => setRequests(prev => prev.filter(request => request.id !== id)))
      .catch(err => setError(err.response?.data?.message || "Error accepting request."));
  };

  const handleDecline = (id) => {
     if (!user || !user.userId) return;
    axios.put(`http://localhost:8080/borrowingRequests/${id}/status`, null, {
        params: { status: 'Rejected', action: 'respond' }, headers: { 'User-Id': user.userId.toString() }
      })
      .then(response => setRequests(prev => prev.filter(request => request.id !== id)))
      .catch(err => setError(err.response?.data?.message || "Error declining request."));
  };


  const renderContent = () => {
      if (isLoading) {
          return <div className="message-area"><CircularProgress /><p style={{marginTop: '10px'}}>Loading requests...</p></div>;
      }
      if (accessDenied) {
          return <div className="message-area"><h2>Access Denied</h2><p>You need to be in owner mode to view received requests.</p></div>;
      }
      if (error && requests.length === 0) { 
          return <div className="message-area"><p className="error-text">Error: {error.toString()}</p></div>;
      }
      if (requests.length === 0) {
          return <div className="message-area"><p>No pending borrowing requests require your attention.</p></div>;
      }
      return (
          <div className="requests-list-container"> 
            {error && <p style={{color: 'red', textAlign: 'center', marginBottom: '1rem'}}>Action failed: {error.toString()}</p>}
            {requests.map((request) => (
            <ReceivedRequestCard
                key={request.id}
                request={request}
                onAccept={handleAccept}
                onDecline={handleDecline}
            />
            ))}
        </div>
      );
  };

  return (
    <div className="page-container received-requests-page-container"> 
        <h1 className="page-title">Incoming Requests</h1>
        {renderContent()}
    </div>
  );
};

export default ReceivedBorrowingRequests;