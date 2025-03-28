import React, { useEffect, useState } from "react";
import Game from "../components/Game";
import './GamePage.css';
import './PageFormat.css';


const GamePage = () => {
    const [games, setGames] = useState([]);

    useEffect(() => {
      fetch("http://localhost:8080/games") // Adjust URL as needed
        .then((response) => response.json())
        .then((data) => setGames(data))
        .catch((error) => console.error("Error fetching games:", error));
    }, []);

return (
    <div className="game-page">
        <h1 className="page-title">Games</h1>
        <div className="games-container">
            {games.map((game) => (
                <Game key={game.id} id={game.id} title={game.name} imageUrl="https://picsum.photos/200/200" />
            ))}
        </div>
    </div>
);
};


export default GamePage;