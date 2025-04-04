import React, { useEffect, useState } from "react";
import { GameHistoryAPI } from "../../GettingAllGamesAPI";
import { useAuth } from "../../AuthContext";
import BorrowedGameCard from "../../components/cards/BorrowedGameCard";
import "./GameHistory.css";

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
        return <div className="game-history-container"><p className="centered">Loading borrowed games history...</p></div>;
    }

    if (error) {
        return <div className="game-history-container"><p className="centered error-message">{error}</p></div>;
    }

    if (history.length === 0) {
        return <div className="game-history-container"><p className="centered">No borrowed games history found.</p></div>;
    }

    return (
        <div className="game-history-container">
            <div className="game-history-list">
                {history.map((request) => (
                    <BorrowedGameCard key={request.id} request={request} />
                ))}
            </div>
        </div>
    );
};

export default GameHistory;
