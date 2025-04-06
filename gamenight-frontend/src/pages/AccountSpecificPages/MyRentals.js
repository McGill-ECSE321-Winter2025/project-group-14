import React, { useState, useEffect, useContext } from "react";
import { Box } from "@mui/material";
import BorrowingRequestItem from '../../components/cards/rentalcard';
import { AuthContext } from "../../AuthContext";

function MyRentals() {
  const { user } = useContext(AuthContext);
  const [authChecked] = useState(true);
  const [borrowedGameCopies, setBorrowedGameCopies] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchBorrowedGameCopies = async () => {
    try {
      const playerResponse = await fetch(`http://localhost:8080/users/${user?.userId}/player-id`, {
        headers: { 
          "Content-Type": "application/json",
          "User-Id": user?.userId 
        }
      });
      
      if (!playerResponse.ok) throw new Error("Failed to fetch player ID");
      const playerId = await playerResponse.json();
  
      const requestsResponse = await fetch(
        `http://localhost:8080/borrowingRequests/${playerId}/status/accepted`,
        {
          headers: { 
            "Content-Type": "application/json",
            "User-Id": user?.userId 
          }
        }
      );
  
      if (!requestsResponse.ok) throw new Error("Failed to fetch borrowed games");
      
      const data = await requestsResponse.json();
      setBorrowedGameCopies(Array.isArray(data) ? data : []);
      
    } catch (error) {
      console.error("Error in fetchBorrowedGameCopies:", error);
      setBorrowedGameCopies([]);
    }
  };
  
  useEffect(() => {
    if (!authChecked || !user) return;

    const loadData = async () => {
      setLoading(true);
      await fetchBorrowedGameCopies();
      setLoading(false);
    };
    loadData();
  }, [authChecked, user]);

  return (
    <div className="rentals-container">
      <div className="rentals-header">
        <h1>My Rentals</h1>
        <p className="subtitle">Your currently active game rentals</p>
      </div>

      {loading ? (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
          <div className="loading-spinner"></div>
        </Box>
      ) : borrowedGameCopies.length === 0 ? (
        <Box 
          display="flex" 
          justifyContent="center" 
          alignItems="center" 
          minHeight="200px"
          sx={{ background: '#f9fafb', borderRadius: '12px' }}
        >
          <p className="empty-state">You don't have any active rentals yet.</p>
        </Box>
      ) : (
        <div className="rentals-grid">
          {borrowedGameCopies.map((gameCopy) => (
            <BorrowingRequestItem 
              key={gameCopy.id}
              request={gameCopy} 
              badgeText="Active Rental"
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default MyRentals;