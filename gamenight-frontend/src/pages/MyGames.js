import React, { useState, useEffect, useContext, useCallback } from "react";
import { Typography, Tabs, Tab, Box, Button, Grid, CircularProgress } from "@mui/material";
import { useNavigate } from "react-router-dom";
import GameCopyCard from "../components/GameCopyCard";
import AddGameCopyForm from "../components/AddGameCopyForm";
import { AuthContext } from "../AuthContext";
import "../App.css";

function MyGamesPage() {
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();
  const [authChecked, setAuthChecked] = useState(false);
  const [tabValue, setTabValue] = useState(0);
  const [myGameCopies, setMyGameCopies] = useState([]);
  const [borrowedGameCopies, setBorrowedGameCopies] = useState([]);
  const [showAddForm, setShowAddForm] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchMyGameCopies = useCallback(async () => {
    try {
      const response = await fetch(`http://localhost:8080/game-copies?owner_id=${user?.userId}`, {
        headers: { "Content-Type": "application/json", "User-Id": user?.userId },
      });
      const data = await response.json();
      setMyGameCopies(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Error fetching game copies:", error);
      setMyGameCopies([]);
    }
  }, [user?.userId]);

  const fetchBorrowedGameCopies = useCallback(async () => {
    try {
      const response = await fetch(`http://localhost:8080/game-copies/borrowed?borrower_id=${user?.userId}`, {
        headers: { "Content-Type": "application/json", "User-Id": user?.userId },
      });
      const data = await response.json();
      setBorrowedGameCopies(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error("Error fetching borrowed games:", error);
      setBorrowedGameCopies([]);
    }
  }, [user?.userId]);

  useEffect(() => {
    if (user === null) {
      navigate('/login', { replace: true });
    } else if (user) {
      setAuthChecked(true);
    }
  }, [user, navigate]);

  useEffect(() => {
    if (!authChecked || !user) return;
    
    const loadData = async () => {
      setLoading(true);
      await Promise.all([fetchMyGameCopies(), fetchBorrowedGameCopies()]);
      setLoading(false);
    };
    loadData();
  }, [authChecked, fetchMyGameCopies, fetchBorrowedGameCopies, user]);

  if (!authChecked) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
        <CircularProgress />
      </Box>
    );
  }

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const handleAddGameCopy = () => {
    setShowAddForm(true);
  };

  const handleCancelAdd = () => {
    setShowAddForm(false);
  };

  const handleGameCopyAdded = async (newGameCopy) => {
    setMyGameCopies(prev => [...prev, newGameCopy]);
    setShowAddForm(false);
  };

  const handleDeleteGameCopy = async (gameCopyId) => {
    try {
      await fetch(`http://localhost:8080/game-copies/${gameCopyId}`, {
        method: "DELETE",
        headers: { "User-Id": user.userId },
      });
      setMyGameCopies(prev => prev.filter(copy => copy.id !== gameCopyId));
    } catch (error) {
      console.error("Error deleting game copy:", error);
    }
  };

  const handleUpdateGameCopy = async (updatedCopy) => {
    try {
      const response = await fetch(`http://localhost:8080/game-copies/${updatedCopy.id}`, {
        method: "PUT",
        headers: { 
          "Content-Type": "application/json",
          "User-Id": user.userId 
        },
        body: JSON.stringify({
          description: updatedCopy.description
        })
      });
      const data = await response.json();
      
      setMyGameCopies(prev => prev.map(copy => 
        copy.id === updatedCopy.id ? data : copy
      ));
    } catch (error) {
      console.error("Error updating game copy:", error);
    }
  };

  return (
    <div className="container">
      <Typography variant="h4" gutterBottom className="centered">
        My Games
      </Typography>

      <Box sx={{ width: '100%', mb: 3 }}>
        <Tabs value={tabValue} onChange={handleTabChange} centered>
          <Tab label="My Collection" />
          <Tab label="Borrowed Games" />
        </Tabs>
      </Box>

      {tabValue === 0 && (
        <>
          {!showAddForm && (
            <Button 
              variant="contained" 
              color="primary" 
              onClick={handleAddGameCopy}
              sx={{ mb: 3 }}
            >
              Add Game Copy
            </Button>
          )}

          {showAddForm && (
            <AddGameCopyForm 
              onCancel={handleCancelAdd} 
              onSuccess={handleGameCopyAdded} 
            />
          )}

          {loading ? (
            <Typography>Loading your game collection...</Typography>
          ) : myGameCopies.length === 0 ? (
            <Typography>You don't have any games in your collection yet.</Typography>
          ) : (
            <Grid container spacing={3}>
              {myGameCopies.map((gameCopy) => (
                <Grid item xs={12} sm={6} md={4} key={gameCopy.id}>
                  <GameCopyCard 
                    gameCopy={gameCopy} 
                    onDelete={handleDeleteGameCopy}
                    onUpdate={handleUpdateGameCopy}
                    isOwner={true}
                  />
                </Grid>
              ))}
            </Grid>
          )}
        </>
      )}

      {tabValue === 1 && (
        <>
          {loading ? (
            <Typography>Loading borrowed games...</Typography>
          ) : borrowedGameCopies.length === 0 ? (
            <Typography>You haven't borrowed any games yet.</Typography>
          ) : (
            <Grid container spacing={3}>
              {borrowedGameCopies.map((gameCopy) => (
                <Grid item xs={12} sm={6} md={4} key={gameCopy.id}>
                  <GameCopyCard 
                    gameCopy={gameCopy} 
                    isOwner={false}
                  />
                </Grid>
              ))}
            </Grid>
          )}
        </>
      )}
    </div>
  );
}

export default MyGamesPage;