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
            const response = await axios.post(
                "http://localhost:8080/games",
                { name, description },
                { headers: getAuthHeaders() }
            );
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
            const storedUser = sessionStorage.getItem("user");
            if (!storedUser) throw new Error("User not logged in");

            const user = JSON.parse(storedUser);
            const userId = user.userId;

            const playerIdResponse = await axios.get(
                `http://localhost:8080/users/${userId}/player-id`,
                { headers: { "User-Id": userId } }
            );
            const playerId = playerIdResponse.data;

            const borrowedGamesResponse = await axios.get(
                `http://localhost:8080/borrowingRequests/${playerId}/status/accepted`,
                { headers: { "User-Id": userId } }
            );

            return borrowedGamesResponse.data;
        } catch (error) {
            console.error("Error fetching borrowed games history:", error);
            return [];
        }
    },

    getGameCopyById: async (gameCopyId) => {
        try {
            const storedUser = sessionStorage.getItem("user");
            if (!storedUser) throw new Error("User not logged in");

            const user = JSON.parse(storedUser);
            const userId = user.userId;

            const response = await axios.get(
                `http://localhost:8080/game-copies/${gameCopyId}`,
                { headers: { "User-Id": userId } }
            );

            return response.data;
        } catch (error) {
            console.error(`Error fetching game copy with ID ${gameCopyId}:`, error);
            return null;
        }
    }
};
