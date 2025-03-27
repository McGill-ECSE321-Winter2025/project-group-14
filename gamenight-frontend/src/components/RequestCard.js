import React from "react";

function RequestCard({ title, status, onViewDetails }) {
    return (
        <div className="request-card">
            <h3>{title}</h3>
            <p>Your Request Was {status}</p>
            <button className="card-button" onClick={onViewDetails}>View Details</button>
        </div>
    );
}

export default RequestCard;
