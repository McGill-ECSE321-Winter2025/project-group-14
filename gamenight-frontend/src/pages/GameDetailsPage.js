import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import './GameDetailsPage.css';
import './PageFormat.css';
import GameReviewsTab from './GameDetailsPageTabs/GameReviewsTab';
import GameCopyCard from "../components/GameCopyGameDetailsPage";


const game = {description: "A FUN GAME"}


const gameCopies = [
  { id: 1, owner: "Alice", description: "Well-maintained copy of Catan with all pieces included." },
  { id: 2, owner: "Bob", description: "Slightly worn Carcassonne set, but still fun to play!" },
  { id: 3, owner: "Charlie", description: "Brand new Ticket to Ride: Europe edition." },
  { id: 4, owner: "Dana", description: "Risk board game, missing one red piece but fully playable." },
];


const GameDetailsPage = () => {

  const { id } = useParams();

  const [activeTab, setActiveTab] = useState('details'); // Track the active tab

  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  // const [game, setGame] = useState();
  
  // useEffect(() => {
  //   fetch(`http://localhost:8080/games/${id}`) // Adjust URL as needed
  //     .then((response) => response.json())
  //     .then((data) => setGame(data))
  //     .catch((error) => console.error("Error fetching game:", error));
  // }, [id]);

  return (
    <div>
      <h1 className="page-title">Game Details for Game ID: {id}</h1>
      <div className='central-image-container'>
        <img className="central-image" src="https://picsum.photos/200/200" alt="game"/>
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
        {activeTab === 'details' && (
          <div>
            <p>{game.description}</p>
          </div>
        )}
        {activeTab === 'reviews' && (
          <div>
            <GameReviewsTab key={id}/>
          </div>
        )}
        {activeTab === 'gameCopies' && (
          <div className='container-center'>
              {gameCopies.map((game) => (
                <GameCopyCard
                  key={game.id}
                  owner={game.owner}
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