import React, { createContext, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { UserManagementAPI } from "./UserManagementAPI";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const navigate = useNavigate(); // Add navigation
    const [loading, setLoading] = useState(true);

    // Load user from sessionStorage when the app starts
    useEffect(() => {
        const storedUser = sessionStorage.getItem("user");
        if (storedUser) {
            setUser(JSON.parse(storedUser));
        } setLoading(false);
    }, []);

    // Function to log in
    const login = async (email, password) => {
        const userData = await UserManagementAPI.loginUser(email, password);
        if (userData) {
           
            sessionStorage.setItem("user", JSON.stringify(userData));
            setUser(userData);
            navigate("/my-games"); // Redirect to My Games
        }
        return userData;
    };

    // Function to log out
    const logout = () => {
        sessionStorage.removeItem("user");
        setUser(null);
        navigate("/"); // Redirect to Home after logout
    };

    return (
        <AuthContext.Provider value={{ user, loading, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
