import axios from "axios";

const API_BASE_URL = "http://localhost:8080/users";

const getAuthHeaders = () => {
    const storedUser = sessionStorage.getItem("user");
    if (storedUser) {
        const user = JSON.parse(storedUser);
        return { "User-Id": user.userId };
    }
    return {};
};

export const UserManagementAPI = {
    registerUser: async (email, password, name) => {
        try {
            const response = await axios.post(API_BASE_URL, {
                emailAdress: email,
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
                sessionStorage.setItem("user", JSON.stringify(response.data));
            }
            return response.data;
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

    updateUser: async (userId, oldPassword, newEmail, newPassword) => {
        try {
            const params = new URLSearchParams();
            if (newEmail) params.append("newEmail", newEmail);
            if (newPassword) params.append("newPassword", newPassword);
            params.append("oldPassword", oldPassword);

            const response = await axios.put(`${API_BASE_URL}/${userId}?${params.toString()}`, {}, {
                headers: getAuthHeaders()
            });
            return response.status === 200;
        } catch (error) {
            console.error("Error updating user:", error);
            return false;
        }
    },

    getAllUsers: async () => {
        try {
            const response = await axios.get(`${API_BASE_URL}`, {
                headers: getAuthHeaders()
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching all users:", error);
            return [];
        }
    },

    getPlayerId: async (userId) => {
        try {
            const response = await axios.get(`http://localhost:8080/players?person_id=${userId}`, {
                headers: getAuthHeaders()
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching player ID:", error);
            return null;
        }
    },

    getUserDetails: async (userId) => {
        try {
            const response = await axios.get(`${API_BASE_URL}/${userId}`, {
                headers: getAuthHeaders()
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching user details:", error);
            return null;
        }
    },
    isActiveOwner: async (userId) => {
        try {
            const response = await axios.get(`${API_BASE_URL}/${userId}/is-owner`, {
                headers: getAuthHeaders()
            });
            return response.data === true;
        } catch (error) {
            console.error("Error checking ownership status:", error);
            return false;
        }
    },

    updateUsername: async (userId, newUsername) => {
        try {
            const response = await axios.put(
                `${API_BASE_URL}/${userId}/username`,
                null,
                {
                    headers: {
                        ...getAuthHeaders(),
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    params: { newUsername }
                }
            );
            return response.status === 200;
        } catch (error) {
            console.error("Error updating username:", error);
            return false;
        }
    }

};
