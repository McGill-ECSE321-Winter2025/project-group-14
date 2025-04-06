import React from 'react';
import './RequestCard.css'; 

function RequestCard({ gameName, status, startTime, endTime }) {

    const getBadgeText = (currentStatus) => {
        switch (currentStatus) {
            case 'Accepted':
                return 'Accepted';
            case 'Rejected':
                return 'Rejected';
            case 'Delivered':
                return 'Pending'; 
            default:
                return status;
        }
    };

    const getBadgeClass = (currentStatus) => {
        switch (currentStatus) {
            case 'Accepted':
                return 'status-accepted';
            case 'Rejected':
                return 'status-rejected';
            case 'Delivered':
                return 'status-pending';
            default:
                return 'status-unknown';
        }
    };

    return (
        <div className="sent-request-card">
            <div className={`request-badge ${getBadgeClass(status)}`}> 
                {getBadgeText(status)}
            </div>
            <div className="username">{gameName || 'Unknown Game'}</div>
            <div className="request-info">
                <p>Status of Your Request: {status}</p>
                <p>Date of Your Request: {startTime} to {endTime}</p>
            </div>
        </div>
    );
}

export default RequestCard;