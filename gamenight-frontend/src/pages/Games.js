import React, { useState, useEffect } from "react";
import GameCard from "../components/GameCard";
import "../App.css";

function Games() {
    const [games, setGames] = useState([]);

    // Simulated API call to fetch games
    useEffect(() => {
        setTimeout(() => {
            setGames([
                { id: 1, title: "Block Wars", image: "https://i.pinimg.com/736x/09/81/88/098188d5e282211d513442f31cfd4ea8.jpg", players: 1200, rating: 95 },
                { id: 2, title: "Tower Defense 2", image: "https://gimgs2.nohat.cc/thumb/f/350/golden-mlg-shrek-face-bling-shrek-dank-meme-funny-wow-bling-meme--comdlpng6947863.jpg", players: 850, rating: 90 },
                { id: 3, title: "Squid Games", image: "https://i.pinimg.com/736x/09/81/88/098188d5e282211d513442f31cfd4ea8.jpg", players: 2100, rating: 88 },
            ]);
        }, 10);
    }, []);

    return (
        <div className="container">
            <h1 className="centered">Games</h1>
            <div className="game-list">
                {games.length > 0 ? (
                    games.map((game) => (
                        <GameCard
                            key={game.id}
                            title={game.title}
                            image={game.image}
                            players={game.players}
                            rating={game.rating}
                        />
                    ))
                ) : (
                    <p>Loading games...</p>
                )}
            </div>
        </div>
    );
}

export default Games;
