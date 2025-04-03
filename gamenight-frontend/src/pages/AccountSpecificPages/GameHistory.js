import React, { useEffect, useState } from "react";
import { GameHistoryAPI } from "../../GettingAllGamesAPI";
import { useAuth } from "../../AuthContext";

const GameHistory = () => {
    const { user, loading } = useAuth();
    const [history, setHistory] = useState([]);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchHistory = async () => {
            try {
                if (!user) {
                    setError("User not logged in.");
                    return;
                }
                const data = await GameHistoryAPI.getBorrowedGamesHistory();
                setHistory(data);
            } catch (err) {
                console.error("Failed to fetch game history", err);
                setError("Failed to load borrowed games history.");
            } finally {
                setIsLoading(false);
            }
        };

        if (!loading) {
            fetchHistory();
        }
    }, [user, loading]);

    if (loading || isLoading) {
        return <div>Loading borrowed games history...</div>;
    }

    if (error) {
        return <div style={{ color: "red" }}>{error}</div>;
    }

    if (history.length === 0) {
        return <div>No borrowed games history found.</div>;
    }

    return (
        <div className="game-history">
            <h2>Borrowed Games History</h2>
            <ul>
                {history.map((request) => {
                    const gameName = request.gameName;
                    const ownerName = request.senderName;

                    return (
                        <li key={request.id} style={{ marginBottom: "1rem" }}>
                            <strong>Game:</strong> {gameName} <br />
                            <strong>From:</strong> {new Date(request.startTime).toLocaleDateString()} <br />
                            <strong>Until:</strong> {new Date(request.endTime).toLocaleDateString()} <br />
                            {ownerName && (
                                <>
                                    <strong>Owner:</strong> {ownerName}
                                </>
                            )}
                        </li>
                    );
                })}
            </ul>
        </div>
    );
};

export default GameHistory;
