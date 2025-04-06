import React, { useState, useEffect, useContext } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import {
  Box,
  CardMedia,
  CircularProgress
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
  }, [id]);


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

  const carouselItems = gameCopies?.map((game) => (
    <div className='game-copy'>
      <GameCopyCard
        key={game.id}
        gameCopyId={game.id}
        owner={game.gameOwnerName}
        description={game.description}
      />
    </div>
  )) || [];

  return (
    <div className='game-page-top-level-container'>
      <h1 className='centered'>{title}</h1>
      <div className='game-page-container'>
        <div className='game-info-container'>
          <div>
            <Box sx={{
              position: 'relative',
              maxWidth: '500px',
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
                    height: '100%',
                    aspectRatio: '1 / 1',
                    objectFit: 'cover',
                    borderRadius: '16px'
                  }}
                />
              )}
            </Box>
          </div>
          <div>
            <p>{game?.description}</p>
          </div>
        </div>

        <div className='game-copies-container'>
          <div>
            <h3 className='centered'>Available game copies</h3>
          </div>
          <div className='game-copies-section'>
            {carouselItems}
          </div>
        </div>
      </div>

      <h2 className='centered'>Reviews</h2>
      <div className='reviews-section'>
        <GameReviewsTab key={id} />
      </div>
    </div>
  );
};

export default GameDetailsPage;