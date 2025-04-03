import React from 'react';

// Component-specific styles
import './GameReview.css';
import '../../styles/card.css';

// Shared styles used in the component
import '../../styles/card.css';
import '../../styles/layout.css';
import '../../styles/animation.css';


const GameReview = ({ author, rating, comment, datePosted }) => {

  const formattedDate = new Date(datePosted).toLocaleDateString();


  return (

    <div className="request-card">
      <div className="card-content">
        <div className="user-section">
          <div className="avatar">
            {author?.charAt(0).toUpperCase()}
          </div>
          <div className="user-details">
            <h3 className="user-name">{author}</h3>
          </div>
          <div className="stars">
            <span>
            {Array.from({ length: 5 }, (_, i) => (
                <span key={i} className={i < Math.round(rating) ? "filled" : ""}>★</span>
            ))}
            </span>
          </div>

        </div>

        <div className="game-section">
          <div className="info-row">
            <span className="info-label">Date posted:</span>
            <span className="info-value"> {formattedDate}</span>
          </div>
          <div className="info-row">
            <span className="comment-value">{comment}</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default GameReview;
