import React from "react";
import { Link } from 'react-router-dom';
import '../../styles/card.css'; // for any card related styles
import GameCard from "./GameCard";

const Game = ({ id, title, rating }) => {
  return (
    <Link to={`/games/${id}`} state={{ title }} style={{ textDecoration: 'none' }}>
      <GameCard gameId={id} title={title} rating={rating} />
    </Link>
  );
};


export default Game;

