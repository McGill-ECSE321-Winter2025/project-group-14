import React, { useState, useEffect } from "react";
import GameCard from "../components/GameCard";
import "../App.css";

function MyGames() {
    const [games, setGames] = useState([]);

    // Simulated API call to fetch games
    useEffect(() => {
        setTimeout(() => {
            setGames([
                { id: 1, title: "Block Wars", image: "https://mediaproxy.tvtropes.org/width/1200/https://static.tvtropes.org/pmwiki/pub/images/500px_shrek_is_love_shrek_is_life_painting.jpg", players: 1200, rating: 95 },
                { id: 2, title: "Tower Defense 2", image: "https://mediaproxy.tvtropes.org/width/1200/https://static.tvtropes.org/pmwiki/pub/images/500px_shrek_is_love_shrek_is_life_painting.jpg", players: 850, rating: 90 },
                { id: 3, title: "Squid Games", image: "https://mediaproxy.tvtropes.org/width/1200/https://static.tvtropes.org/pmwiki/pub/images/500px_shrek_is_love_shrek_is_life_painting.jpg", players: 2100, rating: 88 },
            ]);
        }, 10);
    }, []);

    return (
        <div className="container">
            <h1 className="centered">My Games</h1>
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
                    <p>Loading your games...</p>
                )}
            </div>
        </div>
    );
}

export default MyGames;
