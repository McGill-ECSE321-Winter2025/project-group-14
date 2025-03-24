import axios from "axios";

const API_BASE_URL = "http://localhost:8080/users";

// Get the logged-in user's ID from sessionStorage
const getAuthHeaders = () => {
    const storedUser = sessionStorage.getItem("user");
    if (storedUser) {
        const user = JSON.parse(storedUser);
        return { "User-Id": user.userId };  // Ensure this matches backend expectations
    }
    return {};
};

export const UserManagementAPI = {
    registerUser: async (email, password, name) => {
        try {
            const response = await axios.post(API_BASE_URL, {
                emailAdress: email, // Ensure this matches backend expectations
                password: password,
                name: name,
            });
            return response.status === 201;
        } catch (error) {
            console.error("Error registering user:", error);
            return false;
        }
    },

    loginUser: async (email, password) => {
        try {
            const response = await axios.post(`${API_BASE_URL}/login`, {
                emailAdress: email,
                password: password,
            });
            if (response.data) {
                sessionStorage.setItem("user", JSON.stringify(response.data)); // Store user in sessionStorage
            }
            return response.data; // { userId, email }
        } catch (error) {
            console.error("Login failed:", error);
            return null;
        }
    },

    getUserById: async (userId) => {
        try {
            const response = await axios.get(`${API_BASE_URL}/${userId}`, {
                headers: getAuthHeaders()
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching user:", error);
            return null;
        }
    },

    deleteUser: async (userId) => {
        try {
            const response = await axios.delete(`${API_BASE_URL}/${userId}`, {
                headers: getAuthHeaders()
            });
            return response.status === 200;
        } catch (error) {
            console.error("Error deleting user:", error);
            return false;
        }
    },

    toggleRole: async (userId) => {
        try {
            const response = await axios.put(`${API_BASE_URL}/${userId}/role`, {}, {
                headers: getAuthHeaders()
            });
            return response.status === 200;
        } catch (error) {
            console.error("Error toggling role:", error);
            return false;
        }
    },
};
