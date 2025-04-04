import React, { useState, useEffect, useContext } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import {
  Box,
  CardMedia,
  CircularProgress,
  Tabs,
  Tab
} from "@mui/material";

// Styles
import './GameDetailsPage.css';
import '../../styles/layout.css';
import '../../styles/tabs.css';
import '../../styles/card.css';

// Components
import GameReviewsTab from '../GameDetailsPageTabs/GameReviewsTab';
import GameCopyCard from "./GameCopyGameDetailsPage";
import { AuthContext } from "../../AuthContext";



const GameDetailsPage = () => {

  const { id } = useParams();
  const { user } = useContext(AuthContext);
  const location = useLocation();
  const { title } = location.state || {};
  const [tabValue, setTabValue] = useState(0);

  const handleTabChange = (event, tab) => {
    setTabValue(tab);
  };

  
  const [imageUrl, setImageUrl] = useState(null);
  const [imageLoading, setImageLoading] = useState(true);
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
          const fetchGameImage = async () => {
              try {
                  if (!id) {
                      setImageError(true);
                      return;
                  }
  
                  const response = await fetch(`http://localhost:8080/games/${id}/image`);
                  if (response.ok) {
                      const imageBlob = await response.blob();
                      const url = URL.createObjectURL(imageBlob);
                      setImageUrl(url);
                  } else {
                      setImageError(true);
                  }
              } catch (error) {
                  console.error("Error fetching game image:", error);
                  setImageError(true);
              } finally {
                  setImageLoading(false);
              }
          };
  
          fetchGameImage();
  
          return () => {
              if (imageUrl) {
                  URL.revokeObjectURL(imageUrl);
              }
          };
      }, [id, imageUrl]);


  const [game, setGame] = useState();
  useEffect(() => {
    fetch(`http://localhost:8080/games/${id}`, {
      headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
    })
      .then((response) => response.json())
      .then((data) => setGame(data))
      .catch((error) => console.error("Error fetching game:", error));
  }, [id, user]);


  const [gameCopies, setGameCopies] = useState([]);
  useEffect(() => {
    fetch(`http://localhost:8080/game/${id}/game-copies`, {
      headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
    })
      .then((response) => response.json())
      .then((data) => setGameCopies(data))
      .catch((error) => console.error("Error fetching game:", error));
  }, [id, user]);

  return (
    <div>
      <h1 className="centered">{title}</h1>
      <div>
      <Box sx={{
        position: 'relative',
        maxWidth: '400px',
        height: 'auto',
        backgroundColor: '#f5f5f5',
        overflow: 'hidden',
        margin: '0 auto'
      }}>
        {imageLoading ? (
          <CircularProgress size={24} sx={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)'
          }} />
        ) : (
          <CardMedia
            component="img"
            image={imageError ? '/default-game-image.jpg' : imageUrl}
            alt={title || "Game image"}
            sx={{
              width: '100%',
              height: '100%',
              objectFit: 'contain',
              borderRadius: '16px'
            }}
          />
        )}
      </Box>
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
          <Tab label="Details" />
          <Tab label="Reviews" />
          <Tab label="Game Copies" />
        </Tabs>
      </Box>

      {/* Tab Content */}
      <div className="tab-content">
        {tabValue === 0 && game && (
          <div>
            <p>{game.description}</p>
          </div>
        )}
        {tabValue === 1 && (
          <div>
            <GameReviewsTab key={id} />
          </div>
        )}
        {tabValue === 2 && game && (
          <div>
            {gameCopies?.map((game) => (
              <GameCopyCard
                key={game.id}
                gameCopyId={game.id}
                owner={game.gameOwnerName}
                description={game.description}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default GameDetailsPage;