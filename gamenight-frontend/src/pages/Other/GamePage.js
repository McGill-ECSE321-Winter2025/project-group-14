import React, { useEffect, useState } from "react";
import Game from '../../components/cards/ClickableGame';


const GamePage = () => {
    
    const [games, setGames] = useState([]);
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
            <h1 className="centered">Games</h1>

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
                {filteredGames.length > 0 || query === "" ? (
                    filteredGames.map((game, index) => (
                        <div key={index} className="fade-in-card">
                            <Game id={game.id} title={game.name} rating={Math.round(game.rating * 100)} />
                        </div>
                    ))
                ) : (
                    <p>No results found</p>
                )}
            </div>
        </div>
    );
};


export default GamePage;