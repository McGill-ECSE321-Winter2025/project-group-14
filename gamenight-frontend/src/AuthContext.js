import React, { createContext, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { UserManagementAPI } from "./UserManagementAPI";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const navigate = useNavigate();

    // Load user from sessionStorage when the app starts
    useEffect(() => {
        const storedUser = sessionStorage.getItem("user");
        if (storedUser) {
            setUser(JSON.parse(storedUser));
        }
        setLoading(false);
    }, []);

    const login = async (email, password) => {
        const userData = await UserManagementAPI.loginUser(email, password);
        if (userData) {
            sessionStorage.setItem("user", JSON.stringify(userData));
            setUser(userData);
            navigate("/my-games");
        }
        return userData;
    };

    const logout = () => {
        sessionStorage.removeItem("user");
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, loading, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
