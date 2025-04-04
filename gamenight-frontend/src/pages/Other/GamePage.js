import React, { useEffect, useState } from "react";
import Game from '../../components/cards/ClickableGame';

const GamePage = () => {
  const [games, setGames] = useState([]);
  const [query, setQuery] = useState("");
  const picture = "https://www.francetvinfo.fr/pictures/KI83JKIWxYVA8ng-cUtYxM6l-z8/1200x1200/2016/08/23/shrek-5.jpg";

  useEffect(() => {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!token) {
      console.warn("No token found. User may not be logged in.");
      return;
    }

    fetch("http://localhost:8080/games", {
      headers: {
        Authorization: `Bearer ${token}`,
        "User-Id": userId, // only if your backend requires this
      },
    })
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
        return res.json();
      })
      .then((data) => {
        console.log("Fetched games response:", data);
        if (Array.isArray(data)) {
          setGames(data);
        } else if (Array.isArray(data.games)) {
          setGames(data.games);
        } else {
          console.error("Unexpected response format:", data);
          setGames([]);
        }
      })
      .catch((error) => {
        console.error("Error fetching games:", error.message);
      });
  }, []);

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
        {filteredGames.length > 0 ? (
          filteredGames.map((game) => (
            <div key={game.id} className="fade-in-card">
              <Game
                id={game.id}
                title={game.name}
                rating={Math.round((game.rating || 0) * 100)}
                image={picture}
              />
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
