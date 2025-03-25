import React, { useEffect, useState } from "react";
import Game from "../components/Game";
import './GamePage.css';
import './PageFormat.css';

const games = [
    { id: 1, title: "Mountain View", imageUrl: "https://picsum.photos/200/200" },
    { id: 2, title: "Sunny Beach", imageUrl: "https://picsum.photos/200/200" },
    { id: 3, title: "Forest Path", imageUrl: "https://picsum.photos/200/200" },
    { id: 4, title: "City Skyline", imageUrl: "https://picsum.photos/200/200" },
    { id: 5, title: "Desert Dunes", imageUrl: "https://picsum.photos/200/200" },
    { id: 6, title: "Snowy Peaks", imageUrl: "https://picsum.photos/200/200" },
    { id: 7, title: "Tropical Island", imageUrl: "https://picsum.photos/200/200" },
    { id: 8, title: "Night Sky", imageUrl: "https://picsum.photos/200/200" },
    { id: 9, title: "Mountain View", imageUrl: "https://picsum.photos/200/200" },
    { id: 10, title: "Sunny Beach", imageUrl: "https://picsum.photos/200/200" },
    { id: 11, title: "Forest Path", imageUrl: "https://picsum.photos/200/200" },
    { id: 12, title: "City Skyline", imageUrl: "https://picsum.photos/200/200" },
    { id: 13, title: "Desert Dunes", imageUrl: "https://picsum.photos/200/200" },
    { id: 14, title: "Snowy Peaks", imageUrl: "https://picsum.photos/200/200" },
    { id: 15, title: "Tropical Island", imageUrl: "https://picsum.photos/200/200" },
    { id: 16, title: "Night Sky", imageUrl: "https://picsum.photos/200/200" },
  ];

const GamePage = () => {
    // const [games, setGames] = useState([]);

    // useEffect(() => {
    //   fetch("http://localhost:8080/games") // Adjust URL as needed
    //     .then((response) => response.json())
    //     .then((data) => setGames(data))
    //     .catch((error) => console.error("Error fetching games:", error));
    // }, []);

return (
    <div className="game-page">
        <h1 className="page-title">Games</h1>
        <div className="games-container">
            {games.map((game) => (
                <Game key={game.id} id={game.id} title={game.title} imageUrl="https://picsum.photos/200/200" />
            ))}
        </div>
    </div>
);
};


export default GamePage;