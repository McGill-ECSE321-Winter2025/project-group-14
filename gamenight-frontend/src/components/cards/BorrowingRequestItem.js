import React from 'react';
import PropTypes from 'prop-types';
import '../../App.css';

const BorrowingRequestItem = ({ request, onAccept, onDecline }) => {
  return (
    <div className="request-card">
      <div className="card-content">
        <div className="user-section">
          <div className="avatar">
            {request.senderName?.charAt(0).toUpperCase()}
          </div>
          <div className="user-details">
            <h3 className="user-name">{request.senderName}</h3>
            <span className="request-badge">New Request</span>
          </div>
        </div>


        <div className="game-section">
          <div className="info-row">
            <span className="info-label">Game:</span>
            <span className="info-value">{request.gameName}</span>
          </div>
          <div className="info-row">
            <span className="info-label">Dates:</span>
            <span className="info-value"> {request.startTime} - {request.endTime}</span>
          </div>
        </div>

        <div className="action-buttons">
          <button
            className="btn decline-btn"
            onClick={() => onDecline(request.id)}
          >
            Decline
          </button>
          <button
            className="btn accept-btn"
            onClick={() => onAccept(request.id)}
          >
            Accept
          </button>
        </div>
      </div>
    </div>
  );
};

BorrowingRequestItem.propTypes = {
  request: PropTypes.shape({
    id: PropTypes.number.isRequired,
    senderName: PropTypes.string.isRequired,
    gameName: PropTypes.string.isRequired,
  }).isRequired,
  onAccept: PropTypes.func.isRequired,
  onDecline: PropTypes.func.isRequired,
};

export default BorrowingRequestItem;