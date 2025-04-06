import React from 'react';
import './ReceivedRequestCard.css'; 
import Button from '../ui/Button'; 

function ReceivedRequestCard({ request, onAccept, onDecline }) {

    const senderInitial = (request.senderName && request.senderName.length > 0) 
        ? request.senderName.charAt(0).toUpperCase() 
        : '?';

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        try {
            return new Date(dateString).toLocaleDateString(); 
        } catch (e) { return dateString; }
    };

    return (
        <div className="received-request-card"> 
            <div className="card-avatar-container"> 
                <span className="avatar-initial">{senderInitial}</span>
            </div>

            <div className="card-details-container">
                <div className="card-main-info">
                    <span className="sender-name">{request.senderName || 'Unknown Sender'}</span>
                    <span className="game-name">wants to borrow <strong>{request.gameName || 'Unknown Game'}</strong> from you</span>
                </div>
                <div className="request-dates">
                    Dates: {formatDate(request.startTime)} - {formatDate(request.endTime)}
                </div>
            </div>
            <div className="card-actions-container">
                <Button type="danger" onClick={() => onDecline(request.id)}>Reject</Button>
                <Button type="success" onClick={() => onAccept(request.id)}>Accept</Button>
            </div>
        </div>
    );
}

export default ReceivedRequestCard;