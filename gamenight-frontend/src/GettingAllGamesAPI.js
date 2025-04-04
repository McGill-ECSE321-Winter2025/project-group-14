import axios from "axios";

const API_BASE_URL = "http://localhost:8080/public-random-games";

const getAuthHeaders = () => {
    const storedUser = sessionStorage.getItem("user");
    if (storedUser) {
        const user = JSON.parse(storedUser);
        return { "User-Id": user.userId };
    }
    return {};
};

export const GameAPI = {
    getRandomGames: async () => {
        try {
            const response = await axios.get(API_BASE_URL);
            // scale rating from 0-5 to 0-100
            return response.data.map(game => ({
                ...game,
                rating: game.rating * 20
            }));
        } catch (error) {
            console.error("Error fetching random games:", error);
            return [];
        }
    },

    addGame: async (name, description) => {
        try {
            const response = await axios.post("http://localhost:8080/games", { name, description }, { headers: getAuthHeaders() });
            console.log("Game created:", response.data);
            return response.data;
        } catch (error) {
            console.error("Error creating game:", error);
            return null;
        }
    }
};

export const GameHistoryAPI = {
    getBorrowedGamesHistory: async () => {
        try {
            // Step 1: Get user info
            const storedUser = sessionStorage.getItem("user");
            if (!storedUser) throw new Error("User not logged in");

            const user = JSON.parse(storedUser);
            const userId = user.userId;

            // Step 2: Get Player ID from user ID
            const playerIdResponse = await axios.get(`http://localhost:8080/users/${userId}/player-id`, {
                headers: { "User-Id": userId }
            });
            const playerId = playerIdResponse.data;

            // Step 3: Get Borrowed Game History
            const borrowedGamesResponse = await axios.get(`http://localhost:8080/borrowingRequests/${playerId}/status/accepted`, {
                headers: { "User-Id": userId }
            });

            return borrowedGamesResponse.data;
        } catch (error) {
            console.error("Error fetching borrowed games history:", error);
            return [];
        }
    }
};

