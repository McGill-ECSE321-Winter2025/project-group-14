import React from 'react';
import './GameReview.css';

const GameReview = ({ author, rating, comment, datePosted }) => {
  
  // Format the date
  const formattedDate = new Date(datePosted).toLocaleDateString();

  // Generate stars ratings
  const renderStars = (rating) => {
    const filledStars = '★'.repeat(Math.round(rating)); // Filled stars
    const emptyStars = '☆'.repeat(5 - Math.round(rating)); // Empty stars
    return filledStars + emptyStars; // Combine them
  };

  return (
    <div className="review-card p-4 bg-white rounded-lg shadow-md">
      <div className="review-header flex items-center justify-between mb-3">
        <div className="author-info">
          <h3 className="text-lg font-semibold text-gray-800">{author}</h3>
        </div>
        <div className="rating">
          <span className="text-yellow-500 font-medium">
            {renderStars(rating)}
          </span>
        </div>
      </div>

      <div className="review-date text-sm text-gray-500 mb-3">
        <span>Posted on: {formattedDate}</span>
      </div>

      <div className="review-comment">
        <p className="text-sm text-gray-600">{comment}</p>
      </div>
    </div>
  );
};

export default GameReview;
