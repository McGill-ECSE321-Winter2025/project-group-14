import React, { useState } from "react";
import { GameAPI } from "../GettingAllGamesAPI";

function AddGame() {
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [message, setMessage] = useState("");

    const handleAddGame = async () => {
        if (!name || !description) {
            setMessage("Please provide both name and description.");
            return;
        }
        const result = await GameAPI.addGame(name, description);
        if (result) {
            setMessage("Game added successfully!");
            setName("");
            setDescription("");
        } else {
            setMessage("Failed to add game.");
        }
    };

    return (
        <div style={{ padding: "20px" }}>
            <h2>Add Game (Dev Tool)</h2>
            <div>
                <input
                    type="text"
                    placeholder="Game name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                />
            </div>
            <div>
                <input
                    type="text"
                    placeholder="Game description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                />
            </div>
            <div>
                <button onClick={handleAddGame}>Add Game</button>
            </div>
            <div>{message}</div>
        </div>
    );
}

export default AddGame;
