import React, { useEffect, useState } from "react";
import { GameAPI } from "../../GettingAllGamesAPI";
import GameCard from "../../components/cards/GameCard";

import "../../styles/animation.css";
import "../../pages/Home/Home.css";
import "../../styles/layout.css";
import "../../styles/card.css";
import "../../styles/button.css";
import "../../styles/search.css";
import "../../styles/tabs.css";

function Home() {
    const [games, setGames] = useState([]);
    const [carouselIndex, setCarouselIndex] = useState(0);
    const [fading, setFading] = useState(false);

    useEffect(() => {
        const fetchGames = async () => {
            try {
                const result = await GameAPI.getAllGames();
                setGames(result);
            } catch (err) {
                console.error("Failed to load games for homepage:", err);
            }
        };

        fetchGames();
    }, []);

    // Animate carousel
    useEffect(() => {
        const interval = setInterval(() => {
            setFading(true);
            setTimeout(() => {
                setCarouselIndex((prev) => (prev + 1) % games.length);
                setFading(false);
            }, 400);
        }, 3500);

        return () => clearInterval(interval);
    }, [games]);

    return (
        <div className="marketing-page">
            {/* --- HERO --- */}
            <section className="hero">
                <h1>Level Up Your Board Game Experience</h1>
                <p>Connect. Share. Play. Organize. Your ultimate board game hub.</p>
            </section>

            <h1 className="centered" style={{ marginTop: "2em" }}>Pick your game</h1>

            {/* --- CAROUSEL --- */}
            <div className={`carousel-wrapper ${fading ? "fading-out" : "fading-in"}`}>
                {games.map((game, i) => {
                    const offset = i - carouselIndex;
                    if (offset < -2 || offset > 2) return null;

                    const scale = 1 - Math.abs(offset) * 0.1;
                    const opacity = 1 - Math.abs(offset) * 0.1;
                    const translateX = offset * 300;

                    return (
                        <div
                            onClick={() => window.location.href = "/login"}
                            key={i}
                            className="carousel-item"
                            style={{
                                transform: `translateX(${translateX}px) scale(${scale})`,
                                opacity,
                            }}
                        >
                            <GameCard
                                title={game.name}
                                image={game.image || "https://cdn.mos.cms.futurecdn.net/DCNoD5GWBhpHbkybMGt33X-1000-80.jpg"}
                                rating={game.rating ?? 0}
                            />
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

export default Home;
