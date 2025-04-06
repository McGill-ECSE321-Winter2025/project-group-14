import React from 'react';
import PropTypes from 'prop-types';
import './rentalcard.css';

const rentalcard = ({ 
  request, 
  badgeText = "Active Rental"
}) => {
  const calculateDaysRemaining = (endDate) => {
    const end = new Date(endDate);
    const today = new Date();
    const timeDiff = end - today;
    const daysRemaining = Math.ceil(timeDiff / (1000 * 60 * 60 * 24));
    return daysRemaining > 0 ? daysRemaining : 0;
  };

  const daysRemaining = calculateDaysRemaining(request.endTime);
  const isAlmostOver = daysRemaining <= 3;

  return (
    <div className={`rental-card ${isAlmostOver ? 'warning' : ''}`}>
      <div className="card-content">
        {/* Game Header with Status */}
        <div className="card-header">
          <h3 className="game-title">{request.gameName}</h3>
          <span className={`status-badge ${isAlmostOver ? 'warning' : ''}`}>
            {badgeText}
          </span>
        </div>

        {/* Rental Info */}
        <div className="rental-details">
          <div className="detail-row">
            <span className="detail-label">Rental Period</span>
            <span className="detail-value">
              {new Date(request.startTime).toLocaleDateString()} – {new Date(request.endTime).toLocaleDateString()}
            </span>
          </div>
          
          <div className="detail-row">
            <span className="detail-label">Days Remaining</span>
            <span className={`days-remaining ${isAlmostOver ? 'warning' : ''}`}>
              {daysRemaining} {daysRemaining === 1 ? 'day' : 'days'}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

rentalcard.propTypes = {
  request: PropTypes.shape({
    id: PropTypes.number.isRequired,
    gameName: PropTypes.string.isRequired,
    startTime: PropTypes.string,
    endTime: PropTypes.string,
  }).isRequired,
  badgeText: PropTypes.string,
};

export default rentalcard;