import React from "react";

function ActiveRequestCard({ title, status, onViewDetails }) {
    return (
        <div className="request-card">
            <h3>{title}</h3>
            <button className="card-button" onClick={onViewDetails}>View Details</button>
        </div>
    );
}

export default ActiveRequestCard;
