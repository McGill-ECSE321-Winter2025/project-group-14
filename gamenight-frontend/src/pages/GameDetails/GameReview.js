import React from 'react';

import '../../styles/card-with-user.css';
import '../../styles/card.css';
import '../../styles/layout.css';
import '../../styles/animation.css';
import '../../styles/rating.css';

const GameReview = ({ author, rating, comment, datePosted }) => {

  const formattedDate = new Date(datePosted).toLocaleDateString();


  return (

    <div className="card" style={{ "min-width": "100%" }}>
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

        <div className="shaded-section">
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
