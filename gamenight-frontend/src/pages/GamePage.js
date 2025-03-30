import React, { useEffect, useState } from "react";
import Game from "../components/Game";
import Box from "../components/Box";

const GamePage = () => {
    const [games, setGames] = useState([]);
    const picture = "https://www.francetvinfo.fr/pictures/KI83JKIWxYVA8ng-cUtYxM6l-z8/1200x1200/2016/08/23/shrek-5.jpg";

    useEffect(() => {
      fetch("http://localhost:8080/games") // Adjust URL as needed
        .then((response) => response.json())
        .then((data) => setGames(data))
        .catch((error) => console.error("Error fetching games:", error));
    }, []);

    const [query, setQuery] = useState("");
    const filteredGames = games.filter((game) =>
        game.name.toLowerCase().includes(query.toLowerCase())
    );

return (
    <div className="container">
        <Box>
            <h1 class="centered">Games</h1>

            <div className="centered">
                <input
                    type="text"
                    placeholder="Search for a game..."
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    className="search-input"
                />
            </div>

            <div className="game-list">
                {filteredGames.length > 0 ? (
                    filteredGames.map((game, index) => (
                        <div key={index} className="fade-in-card">
                            <Game key={game.id} id={game.id} title={game.name} rating={game.rating} image={picture} />
                        </div>
                    ))
                ):(
                    <p>No results found</p>
                )}
            </div>
        </Box>
    </div>
);
};


export default GamePage;