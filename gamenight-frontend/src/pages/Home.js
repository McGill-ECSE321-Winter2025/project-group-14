import React from "react";
import GameCard from "../components/GameCard";

function Home() {
    const featuredGames = [
        { title: "Dead Rails [Alpha]", image: "https://via.placeholder.com/100" },
        { title: "RIVALS", image: "https://via.placeholder.com/100" },
        { title: "The Hunt: Mega Edition", image: "https://via.placeholder.com/100" }
    ];

    return (
        <div>
            <h1>Featured Games</h1>
            <div style={{ display: "flex", flexWrap: "wrap" }}>
                {featuredGames.map((game, index) => (
                    <GameCard key={index} title={game.title} image={game.image} />
                ))}
            </div>
        </div>
    );
}

export default Home;
