import React, { useState, useEffect, useContext } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import './GameDetailsPage.css';
import GameReviewsTab from './GameReviewsTab';
import '../../styles/tabs.css';
import GameCopyCard from '../../components/cards/GameCopyCard';
import { AuthContext } from "../../AuthContext";

const GameDetailsPage = () => {
  const { id } = useParams();
  const { user } = useContext(AuthContext);
  const [activeTab, setActiveTab] = useState('details');

  const location = useLocation();
  const { title, image } = location.state || {};

  const [game, setGame] = useState(null);
  const [gameCopies, setGameCopies] = useState([]);

  useEffect(() => {
    fetch(`http://localhost:8080/games/${id}`, {
      headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
    })
      .then((response) => response.json())
      .then((data) => setGame(data))
      .catch((error) => console.error("Error fetching game:", error));
  }, [id, user]);

  useEffect(() => {
    fetch(`http://localhost:8080/game/${id}/game-copies`, {
      headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
    })
      .then((response) => response.json())
      .then((data) => setGameCopies(data))
      .catch((error) => console.error("Error fetching game copies:", error));
  }, [id, user]);

  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  // Dummy handlers to be replaced with actual logic
  const handleDeleteCopy = (copyId) => {
    setGameCopies(prev => prev.filter(copy => copy.id !== copyId));
  };

  const handleUpdateCopy = (updatedCopy) => {
    setGameCopies(prev => prev.map(copy => copy.id === updatedCopy.id ? updatedCopy : copy));
  };

  return (
    <div className='container'>
      <h1 className="centered">{title}</h1>
      <div className='central-image-container'>
        <img className="central-image" src={image} alt="game" />
      </div>

      {/* Tab Navigation */}
      <div className="tabs">
        <button className={`tab ${activeTab === 'details' ? 'active' : ''}`} onClick={() => handleTabChange('details')}>
          Game Details
        </button>
        <button className={`tab ${activeTab === 'reviews' ? 'active' : ''}`} onClick={() => handleTabChange('reviews')}>
          Reviews
        </button>
        <button className={`tab ${activeTab === 'gameCopies' ? 'active' : ''}`} onClick={() => handleTabChange('gameCopies')}>
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
            {gameCopies.map((gameCopy) => (
              <GameCopyCard
                key={gameCopy.id}
                gameCopy={gameCopy}
                isOwner={user.userId === gameCopy.personId}
                onDelete={handleDeleteCopy}
                onUpdate={handleUpdateCopy}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default GameDetailsPage;
