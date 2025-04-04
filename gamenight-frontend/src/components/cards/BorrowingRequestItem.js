import React from 'react';
import PropTypes from 'prop-types';
import './BorrowingRequestItem.css';
import '../../styles/card-with-user.css'

const BorrowingRequestItem = ({ 
  request, 
  onAccept, 
  onDecline, 
  badgeText = "New Request"  // Kept for badge customization
}) => {
  // Always show borrower (sender) info - no more owner toggle
  const displayName = request.senderName;
  const displayInitial = displayName?.charAt(0).toUpperCase() || '?';
  
  return (
    <div className="card" style={{ "max-width": "420px" }}>
      <div className="card-content">
        {/* Borrower Info Section (Kept) */}
        <div className="user-section">
          <div className="avatar">
            {displayInitial}
          </div>
          <div className="user-details">
            <h3 className="user-name">{displayName || 'Borrower'}</h3>
            <span className="request-badge">{badgeText}</span>
          </div>
        </div>

        {/* Game Info Section */}
        <div className="shaded-section">
          <div className="info-row">
            <span className="info-label">Game:</span>
            <span className="info-value">{request.gameName}</span>
          </div>
          <div className="info-row">
            <span className="info-label">Dates:</span>
            <span className="info-value">{request.startTime} - {request.endTime}</span>
          </div>
        </div>

        {/* Action Buttons (if provided) */}
        {onAccept && onDecline && (
          <div className="action-buttons">
            <button className="btn decline-btn" onClick={() => onDecline(request.id)}>
              Decline
            </button>
            <button className="btn accept-btn" onClick={() => onAccept(request.id)}>
              Accept
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

BorrowingRequestItem.propTypes = {
  request: PropTypes.shape({
    id: PropTypes.number.isRequired,
    senderName: PropTypes.string,  // Kept
    gameName: PropTypes.string.isRequired,
    startTime: PropTypes.string,
    endTime: PropTypes.string,
  }).isRequired,
  onAccept: PropTypes.func,
  onDecline: PropTypes.func,
  badgeText: PropTypes.string,
};

export default BorrowingRequestItem;