import React from 'react';
import '../../styles/card.css';
import '../../styles/layout.css';
import '../../styles/animation.css';
import './MyReviewsCard.css';

const MyReviewsCard = ({ rating, comment, datePosted, gameName }) => {
  const formattedDate = new Date(datePosted).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });

  return (
    <div className="mygamesreview-card"> 
      <div className="card-content">
        <div className="header-section">
          <h3 className="review-game">{gameName || 'Untitled Game'}</h3>
          <div className="stars-container">
            {Array.from({ length: 5 }, (_, i) => (
              <span 
                key={i} 
                className={`star ${i < Math.round(rating) ? "filled" : ""}`}
              >
                ★
              </span>
            ))}
          </div>
          <div className="review-date">
            Posted on: {formattedDate}
          </div>
        </div>

        <div className="review-content">
          <div className="review-comment">
            {comment}
          </div>
        </div>
      </div>
    </div>
  );
};

export default MyReviewsCard;