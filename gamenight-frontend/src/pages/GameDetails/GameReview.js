import React from 'react';

// Component-specific styles
import './GameReview.css';

// Shared styles used in the component
import '../../styles/card.css';
import '../../styles/layout.css';
import '../../styles/animation.css';


const GameReview = ({ author, rating, comment, datePosted }) => {

  const formattedDate = new Date(datePosted).toLocaleDateString();

  // Generate stars ratings
  const renderStars = (rating) => {
    const filledStars = '★'.repeat(Math.round(rating));
    const emptyStars = '☆'.repeat(5 - Math.round(rating));
    return filledStars + emptyStars;
  };

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
          <div className="rating">
            <span>
              {renderStars(rating)}
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
