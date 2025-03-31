import React, { useState, useEffect } from "react";
import GameCard from "../components/GameCard";
import Box from "../components/Box";

function Home() {
    const featuredGames = [
        { title: "Dead Shrek [Alpha]", image: "https://www.francetvinfo.fr/pictures/KI83JKIWxYVA8ng-cUtYxM6l-z8/1200x1200/2016/08/23/shrek-5.jpg" },
        { title: "Amazing Shrek", image: "https://www.francetvinfo.fr/pictures/KI83JKIWxYVA8ng-cUtYxM6l-z8/1200x1200/2016/08/23/shrek-5.jpg" },
        { title: "The Hunt: Shrek's Edition", image: "https://www.francetvinfo.fr/pictures/KI83JKIWxYVA8ng-cUtYxM6l-z8/1200x1200/2016/08/23/shrek-5.jpg" }
    ];

    const [visibleGames, setVisibleGames] = useState([]);

    useEffect(() => {
        if (visibleGames.length < featuredGames.length) {
            const timeout = setTimeout(() => {
                setVisibleGames((prev) => [...prev, featuredGames[prev.length]]);
            }, 100);

            return () => clearTimeout(timeout);
        }
    }, [visibleGames]);

    return (
        <div className="container">
            <Box>
                <h1 class="centered">Featured Games</h1>
                <div className="game-list">
                    {visibleGames.map((game, index) => (
                        <div key={index} className="fade-in-card">
                            <GameCard title={game.title} image={game.image} />
                        </div>
                    ))}
                </div>
            </Box>
        </div>
    );
}

export default Home;
