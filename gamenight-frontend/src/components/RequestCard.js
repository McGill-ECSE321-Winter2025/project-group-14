// RequestCard.js
import React from "react";

function RequestCard({ title, status, onViewDetails }) {
    return (
        <div className="request-card">
            <h3>{title}</h3>
            <p>Status: {status}</p>
            <button className="card-button" onClick={onViewDetails}>View Details</button>
        </div>
    );
}

export default RequestCard;
