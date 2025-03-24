import React from "react";

function GameCard({ title, image }) {
    return (
        <div style={{ border: "1px solid #ccc", padding: "10px", margin: "10px" }}>
            <img src={image} alt={title} style={{ width: "100px", height: "100px" }} />
            <h3>{title}</h3>
        </div>
    );
}

export default GameCard;
