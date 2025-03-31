import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import BorrowingRequestItem from '../components/BorrowingRequestItem';
import { AuthContext } from '../AuthContext';

const ReceivedBorrowingRequests = () => {
  const { user, loading } = useContext(AuthContext);
  const navigate = useNavigate();
  const [requests, setRequests] = useState([]);
  const [error, setError] = useState(null);
  const [ownerId, setOwnerId] = useState(null);

  useEffect(() => {
    if (loading || !user) return;
    
    const token = localStorage.getItem('token');
    
    const fetchOwnerIdAndRequests = async () => {
      try {
        // First fetch the ownerId for this user
        const ownerIdResponse = await axios.get(
          `http://localhost:8080/users/${user.userId}/owner-id`,
          {
            headers: { 
              Authorization: `Bearer ${token}`,
              'User-Id': user.userId.toString()
            }
          }
        );
        
        const fetchedOwnerId = ownerIdResponse.data;
        setOwnerId(fetchedOwnerId);
        console.log('Fetched ownerId:', fetchedOwnerId);

        // Now fetch requests with the ownerId
        const response = await axios.get(
          `http://localhost:8080/borrowingRequests/owners/${fetchedOwnerId}/lending-history`,
          {
            headers: { 
              Authorization: `Bearer ${token}`,
              'User-Id': user.userId.toString()
            }
          }
        );
        
        console.log('Response Data:', response.data);
        
        console.log('Raw Response Data:', response.data);

        const deliveredRequests = response.data.filter(request => {
          console.log('Request status:', request.status, typeof request.status);
          return request.status === 'Delivered' || request.status === 0;
        });
        
        setRequests(deliveredRequests);
      } catch (err) {
        console.error('Error:', err);
        setError(err.response?.data?.message || err.message);
      }
    };
  
    fetchOwnerIdAndRequests();
  }, [loading, user]);

  const handleAccept = (id) => {
    const token = localStorage.getItem('token');
    axios
      .put(`http://localhost:8080/borrowingRequests/${id}/status`, null, {
        params: { status: 'Accepted', action: 'respond' },
        headers: { Authorization: `Bearer ${token}`,
        'User-Id': user.userId.toString() }
      })
      .then(response => {
        console.log('Accepted request with ID:', id);
        setRequests(prev => prev.filter(request => request.id !== id));
      })
      .catch(err => console.error('Error accepting request:', err));
  };

  const handleDecline = (id) => {
    const token = localStorage.getItem('token');
    axios
      .put(`http://localhost:8080/borrowingRequests/${id}/status`, null, {
        params: { status: 'Rejected', action: 'respond' },
        headers: { Authorization: `Bearer ${token}`,
        'User-Id': user.userId.toString() }
      })
      .then(response => {
        console.log('Declined request with ID:', id);
        setRequests(prev => prev.filter(request => request.id !== id));
      })
      .catch(err => console.error('Error declining request:', err));
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error.toString()}</div>;
  }

  return (
    <div style={{ padding: '20px' }}>
      <h1>Received Borrowing Requests</h1>
      {requests.length === 0 ? (
        <p>No borrowing requests available.</p>
      ) : (
        requests.map((request) => (
          <BorrowingRequestItem
            key={request.id}
            request={request}
            onAccept={handleAccept}
            onDecline={handleDecline}
          />
        ))
      )}
    </div>
  );
};

export default ReceivedBorrowingRequests;