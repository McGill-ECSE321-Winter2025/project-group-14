import React from "react";
import GameCard from "../components/GameCard";

function Home() {
    const featuredGames = [
        { title: "Dead Shreck [Alpha]", image: "https://www.francetvinfo.fr/pictures/KI83JKIWxYVA8ng-cUtYxM6l-z8/1200x1200/2016/08/23/shrek-5.jpg" },
        { title: "Amazing Shrkek", image: "https://www.francetvinfo.fr/pictures/KI83JKIWxYVA8ng-cUtYxM6l-z8/1200x1200/2016/08/23/shrek-5.jpg" },
        { title: "The Hunt: Shreks Edition", image: "https://www.francetvinfo.fr/pictures/KI83JKIWxYVA8ng-cUtYxM6l-z8/1200x1200/2016/08/23/shrek-5.jpg" }
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
