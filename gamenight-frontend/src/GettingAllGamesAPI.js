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
            return response.data;
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
