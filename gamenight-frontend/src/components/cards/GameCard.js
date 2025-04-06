import React, { useState, useEffect } from "react";
import '../../styles/card.css';
import '../../styles/rating.css'
import {
    Box,
    CardMedia,
    CircularProgress
} from "@mui/material";

function GameCard({ gameId, title, rating }) {
    // Convert rating (%) into 0-5 stars
    const maxStars = 5;
    const starCount = Math.round((rating / 100) * maxStars);

    const [imageUrl, setImageUrl] = useState(null);
    const [imageLoading, setImageLoading] = useState(true);
    const [imageError, setImageError] = useState(false);

    useEffect(() => {
        const fetchGameImage = async () => {
            try {
                if (!gameId) {
                    setImageError(true);
                    return;
                }

                const response = await fetch(`http://localhost:8080/games/${gameId}/image`);
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
    }, [gameId]);


    return (
        <div className="game-card fade-in-card">
            <Box sx={{
                position: 'relative',
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
                            objectFit: 'cover'
                        }}
                    />
                )}
            </Box>
            <div className="game-card-content">
                <h3>{title}</h3>
                {(starCount >= 0) ? (
                    <div className="stars">
                        {Array.from({ length: maxStars }, (_, i) => (
                            <span key={i} className={i < starCount ? "filled" : ""}>★</span>
                        ))}
                    </div>
                ) : (
                    <div></div>
                )}

            </div>
        </div>
    );
}

export default GameCard;
