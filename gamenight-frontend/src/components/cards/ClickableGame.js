import React from "react";
import { Link } from 'react-router-dom';
import '../../App.css';
import GameCard from "./GameCard";

const Game = ({ id, title, image, rating }) => {
  return (
    <Link to={`/games/${id}`} state={{ title, image }} style={{ textDecoration: 'none' }}>
      <GameCard title={title} image={image} rating={rating} />
    </Link>
  );
};


export default Game;

