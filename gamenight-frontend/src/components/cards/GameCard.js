import React from "react";
import '../../styles/card.css';

function GameCard({ title, image, rating }) {
    // Convert rating (%) into 0-5 stars
    const maxStars = 5;
    const starCount = Math.round((rating / 100) * maxStars);

    return (
        <div className="game-card fade-in-card">
            <div className="game-card-image">
                <img src={image} alt={title} />
            </div>
            <div className="game-card-content">
                <h3>{title}</h3>
                <div className="game-card-stars">
                    {Array.from({ length: maxStars }, (_, i) => (
                        <span key={i} className={i < starCount ? "filled" : ""}>★</span>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default GameCard;
