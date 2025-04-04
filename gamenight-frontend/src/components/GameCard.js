import React from "react";
import Button from "./Button";
import "../App.css";

function GameCard({ title, image, players, rating }) {
    return (
        <div className="game-card">
            <div className="game-card-image">
                <img src={image} alt={title} />
            </div>
            <div className="game-card-content">
                <h3>{title}</h3>
                <p>Players: {players}</p>
                <p>Rating: {rating}%</p>
            </div>
            <div className="game-card-actions">
                <Button rounded>▶ Play</Button>
            </div>
        </div>
    );
}

export default GameCard;
