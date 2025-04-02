import React, { createContext, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { UserManagementAPI } from "./UserManagementAPI";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    // Load user from sessionStorage when the app starts
    useEffect(() => {
        const storedUser = sessionStorage.getItem("user");
        if (storedUser) {
            setUser(JSON.parse(storedUser));
            console.log("Loaded user from sessionStorage:", JSON.parse(storedUser));
        }
    }, []);

    // Function to log in
    const login = async (email, password) => {
        try {
            const userData = await UserManagementAPI.loginUser(email, password);
            if (userData) {
                const personId = userData.userId;  // Treating userId as personId
                sessionStorage.setItem("user", JSON.stringify({ userId: personId, username: userData.username }));
                setUser({ userId: personId, username: userData.username });
                console.log("Logged in with user ID:", personId);
                navigate("/my-games");
            } else {
                console.error("Login failed: No user data returned.");
            }
        } catch (error) {
            console.error("Login error:", error);
        }
    };
    

    // Function to log out
    const logout = () => {
        sessionStorage.removeItem("user");
        setUser(null);
        navigate("/");
    };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
