import React, { useState, useEffect, useContext } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import './GameDetailsPage.css';
import GameReviewsTab from './GameReviewsTab';
import '../../App.css';
import GameCopyCard from "./GameCopyGameDetailsPage";
import { AuthContext } from "../../AuthContext";

const GameDetailsPage = () => {

  const { id } = useParams();
  const { user } = useContext(AuthContext);
  const [activeTab, setActiveTab] = useState('details'); // Track the active tab

  const location = useLocation();
  const { title, image } = location.state || {};

  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  const [game, setGame] = useState();

  useEffect(() => {
    fetch(`http://localhost:8080/games/${id}`, {
      headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
    })
      .then((response) => response.json())
      .then((data) => setGame(data))
      .catch((error) => console.error("Error fetching game:", error));
  }, [id, user]);

  const [gameCopies, setGameCopies] = useState();

  useEffect(() => {
    fetch(`http://localhost:8080/game/${id}/game-copies`, {
      headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
    })
      .then((response) => response.json())
      .then((data) => setGameCopies(data))
      .catch((error) => console.error("Error fetching game:", error));
  }, [id, user]);

  return (
    <div className='container'>
      <h1 className="centered">{title}</h1>
      <div className='central-image-container'>
        <img className="central-image" src={image} alt="game" />
      </div>


      {/* Tab Navigation */}
      <div className="tabs">
        <button
          className={`tab ${activeTab === 'details' ? 'active' : ''}`}
          onClick={() => handleTabChange('details')}
        >
          Game Details
        </button>
        <button
          className={`tab ${activeTab === 'reviews' ? 'active' : ''}`}
          onClick={() => handleTabChange('reviews')}
        >
          Reviews
        </button>
        <button
          className={`tab ${activeTab === 'gameCopies' ? 'active' : ''}`}
          onClick={() => handleTabChange('gameCopies')}
        >
          Game Copies
        </button>
      </div>

      {/* Tab Content */}
      <div className="tab-content">
        {activeTab === 'details' && game && (
          <div>
            <p>{game.description}</p>
          </div>
        )}
        {activeTab === 'reviews' && (
          <div>
            <GameReviewsTab key={id} />
          </div>
        )}
        {activeTab === 'gameCopies' && (
          <div className='container-center'>
            {gameCopies.map((game) => (
              <GameCopyCard
                key={game.id}
                gameCopyId={game.id}
                owner={game.gameOwnerName}
                description={game.description}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default GameDetailsPage;