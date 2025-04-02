import React, { useState, useEffect, useContext, useCallback } from "react";
import { Tabs, Tab, Box, Modal, Backdrop, Fade } from "@mui/material";
import GameCopyCard from "../../components/cards/GameCopyCard";
import AddGameCopyForm from "../../components/forms/AddGameCopyForm";
import { AuthContext } from "../../AuthContext";
import Button from "../../components/ui/Button";
import BorrowingRequestItem from '../../components/cards/BorrowingRequestItem';



function MyGamesPage() {
  const { user } = useContext(AuthContext);
  const [authChecked] = useState(true);
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
      const playerResponse = await fetch(`http://localhost:8080/players?person_id=${user?.userId}`, {
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
  }, [user?.userId]);
  
  useEffect(() => {
    if (!authChecked || !user) return;

    const loadData = async () => {
      setLoading(true);
      await Promise.all([
        fetchMyGameCopies(), 
        fetchBorrowedGameCopies(), 
      ]);
      setLoading(false);
    };
    loadData();
  }, [authChecked, fetchMyGameCopies, fetchBorrowedGameCopies, user]);
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

  const modalStyle = {
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 400,
    bgcolor: 'background.paper',
    boxShadow: 24,
    p: 4,
    borderRadius: 2
  };

  return (
    <div className="container">
      <div>
        <h1 className="centered">My Games</h1>
      </div>

      <Box sx={{ 
        width: '100%', 
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
          '&:hover': { color: 'black', opacity: 1 }
        }
      }}>
        <Tabs value={tabValue} onChange={handleTabChange} centered variant="fullWidth">
          <Tab label="My Collection" />
          <Tab label="Borrowed Games" />
        </Tabs>
      </Box>

      {tabValue === 0 && (
        <>
          <Box display="flex" justifyContent="center" sx={{ mb: 3 }}>
            <Box sx={{ display: "inline-block" }}>
              <Button onClick={handleAddGameCopy}>Add Game Copy</Button>
            </Box>
          </Box>

          <Modal
            open={showAddForm}
            onClose={handleCancelAdd}
            closeAfterTransition
            BackdropComponent={Backdrop}
            BackdropProps={{ timeout: 500 }}
          >
            <Fade in={showAddForm}>
              <Box sx={modalStyle}>
                <AddGameCopyForm
                  onCancel={handleCancelAdd}
                  onSuccess={handleGameCopyAdded}
                />
              </Box>
            </Fade>
          </Modal>

          {loading ? (
            <Box display="flex" justifyContent="center">
              <p className="text-center">Loading your game collection...</p>
            </Box>
          ) : myGameCopies.length === 0 ? (
            <Box display="flex" justifyContent="center">
              <p className="text-center">You don't have any games in your collection yet.</p>
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
              {myGameCopies.map((gameCopy) => (
                <Box key={gameCopy.id} sx={{ 
                  width: { xs: '100%', sm: 'calc(50% - 8px)', md: 'calc(33.333% - 11px)' },
                  maxWidth: '280px'
                }}>
                  <GameCopyCard 
                    gameCopy={gameCopy} 
                    onDelete={handleDeleteGameCopy}
                    onUpdate={handleUpdateGameCopy}
                    isOwner={true}
                  />
                </Box>
              ))}
            </Box>
          )}
        </>
      )}

      {tabValue === 1 && (
        <>
          {loading ? (
            <Box display="flex" justifyContent="center">
              <p className="text-center">Loading borrowed games...</p>
            </Box>
          ) : borrowedGameCopies.length === 0 ? (
            <Box display="flex" justifyContent="center">
              <p className="text-center">You haven't borrowed any games yet.</p>
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
                    badgeText="Active Borrow"
                  />
                </Box>
              ))}
            </Box>
          )}
        </>
      )}
    </div>
  );
}

export default MyGamesPage;