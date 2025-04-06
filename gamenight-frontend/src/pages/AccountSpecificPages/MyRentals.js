import React, { useState, useEffect, useContext } from "react";
import { Box } from "@mui/material";
import BorrowingRequestItem from '../../components/cards/BorrowingRequestItem';
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
    <div className="container">
      <div>
        <h1 className="centered">My Rentals</h1>
      </div>

      {loading ? (
        <Box display="flex" justifyContent="center">
          <p className="text-center">Loading your rentals...</p>
        </Box>
      ) : borrowedGameCopies.length === 0 ? (
        <Box display="flex" justifyContent="center">
          <p className="text-center">You don't have any active rentals.</p>
        </Box>
      ) : (
        <Box sx={{ 
          display: 'flex',
          justifyContent: 'center',
          flexWrap: 'wrap',
          gap: '16px',
          maxWidth: '1200px',
          margin: '0 auto',
          padding: '0 16px'
        }}>
          {borrowedGameCopies.map((gameCopy) => (
            <Box key={gameCopy.id} sx={{ 
              width: { xs: '100%', sm: 'calc(50% - 8px)', md: 'calc(33.333% - 11px)' },
              maxWidth: '280px'
            }}>
              <BorrowingRequestItem 
                request={gameCopy} 
                badgeText="Active Rental"
              />
            </Box>
          ))}
        </Box>
      )}
    </div>
  );
}

export default MyRentals;