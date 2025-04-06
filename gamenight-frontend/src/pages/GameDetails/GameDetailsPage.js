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
  const [carouselIndex, setCarouselIndex] = useState(0);

  useEffect(() => {
    fetch(`http://localhost:8080/game/${id}/game-copies`, {
      headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
    })
      .then((response) => response.json())
      .then((data) => {
        setGameCopies(data);
        // ✅ Center the 3rd card if we have enough, otherwise default to 0
        setCarouselIndex(data.length >= 3 ? 1 : 0);
        if (data.length >= 5) setCarouselIndex(2);

      })
      .catch((error) => console.error("Error fetching game:", error));
  }, [id, user]);


  const handleWheel = (e) => {
    e.preventDefault();
    if (e.deltaY > 0 && carouselIndex < gameCopies.length - 1) {
      setCarouselIndex((prev) => prev + 1);
    } else if (e.deltaY < 0 && carouselIndex > 0) {
      setCarouselIndex((prev) => prev - 1);
    }
  };

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
          <div className='game-copies-section' onWheel={handleWheel}>
            {gameCopies.map((copy, i) => {
              const offset = i - carouselIndex;
              if (Math.abs(offset) > 3) return null;

              const scale = 1 - Math.abs(offset) * 0.1;
              const opacity = 1 - Math.abs(offset) * 0.25;
              const translateX = offset * 320;

              return (
                <div
                  key={copy.id}
                  className="carousel-copy-item"
                  style={{
                    left: '50%',
                    transform: `translateX(${offset * 350 - 200}px) scale(${scale})`,
                    opacity,
                    zIndex: 10 - Math.abs(offset),
                    width: '400px',
                    position: 'absolute',
                    transformOrigin: 'center center',
                    transition: 'transform 0.6s ease, opacity 0.6s ease'
                  }}
                >
                  <GameCopyCard
                    gameCopyId={copy.id}
                    owner={copy.gameOwnerName}
                    description={copy.description}
                  />
                </div>
              );
            })}
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
