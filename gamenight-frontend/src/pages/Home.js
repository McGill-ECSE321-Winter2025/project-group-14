import React, { useEffect, useState } from "react";
import { GameAPI } from "../GettingAllGamesAPI";
import GameCard from "../components/GameCard";
import "../App.css";

function Home() {
    const [games, setGames] = useState([]);
    const [carouselIndex, setCarouselIndex] = useState(2); // 🟣 Start with index 2 centered
    const [fading, setFading] = useState(false);

    useEffect(() => {
        fetchGames();
    }, []);

    useEffect(() => {
        if (games.length < 10) return; // avoid animation if no data yet

        const interval = setInterval(() => {
            if (carouselIndex >= 7) { // ✅ leave early
                setFading(true);
                setTimeout(() => {
                    fetchGames();
                    setCarouselIndex(2); // ✅ restart with 3rd card centered
                    setFading(false);
                }, 800);
            } else {
                setCarouselIndex(prev => prev + 1);
            }
        }, 1000);

        return () => clearInterval(interval);
    }, [carouselIndex, games.length]);

    const fetchGames = async () => {
        const randomGames = await GameAPI.getRandomGames();
        setGames(randomGames);
    };

    return (
        <div className={`carousel-wrapper ${fading ? "fading-out" : "fading-in"}`}>
            {games.map((game, i) => {
                const offset = i - carouselIndex;

                // ✅ only render visible ones
                if (offset < -2 || offset > 2) return null;

                const scale = 1 - Math.abs(offset) * 0.15;
                const opacity = 1 - Math.abs(offset) * 0.25;
                const translateX = offset * 250; // adjust spacing

                return (
                    <div
                        key={i}
                        className="carousel-item"
                        style={{
                            transform: `translateX(${translateX}px) scale(${scale})`,
                            opacity,
                        }}
                    >
                        <GameCard
                            title={game.name}
                            image={"https://cdn.mos.cms.futurecdn.net/DCNoD5GWBhpHbkybMGt33X-1000-80.jpg"}
                            rating={game.rating}
                        />
                    </div>
                );
            })}
        </div>
    );
}

export default Home;
