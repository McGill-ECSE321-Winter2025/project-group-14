import React from "react";
import './Game.css';
import { Link } from 'react-router-dom';

const Game = ({ id, title, imageUrl }) => {
    return (
        <Link to={`/games/${id}`} state={{ title }} className="game-card" style={{ textDecoration: 'none' }}>
            <div className="image-container">
            <img src={imageUrl} alt={title} />
            </div>
            <h2 className="game-title">{title}</h2>
        </Link>
    );
  };
  

export default Game;

