import React, { createContext, useState, useEffect, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { UserManagementAPI } from "./UserManagementAPI";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isOwner, setIsOwner] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchData = async () => {
            const storedUser = sessionStorage.getItem("user");
            if (storedUser) {
                const parsedUser = JSON.parse(storedUser);
                setUser(parsedUser);

                try {
                    const active = await UserManagementAPI.isActiveOwner(parsedUser.userId);
                    setIsOwner(active);
                } catch (err) {
                    console.warn("Could not determine if user is owner.", err);
                    setIsOwner(false);
                }
            }
            setLoading(false);
        };

        fetchData();
    }, []);

    const refreshIsOwner = async () => {
        if (!user) return;
        try {
            const result = await UserManagementAPI.isActiveOwner(user.userId);
            setIsOwner(result);
        } catch (e) {
            console.error("Error refreshing owner status:", e);
            setIsOwner(false);
        }
    };

    const login = async (email, password) => {
        try {
            const userData = await UserManagementAPI.loginUser(email, password);
            if (userData) {
                sessionStorage.setItem("user", JSON.stringify(userData));
                setUser(userData);

                try {
                    const active = await UserManagementAPI.isActiveOwner(userData.userId);
                    setIsOwner(active);
                } catch (err) {
                    console.warn("Could not determine if user is owner after login.", err);
                    setIsOwner(false);
                }

                navigate("/my-games");
            } else {
                console.error("Login failed: No user data returned.");
            }
        } catch (error) {
            console.error("Login error:", error);
        }
    };

    const logout = () => {
        sessionStorage.removeItem("user");
        setUser(null);
        setIsOwner(false);
        navigate("/");
    };

    return (
        <AuthContext.Provider value={{ user, setUser, loading, login, logout, isOwner, refreshIsOwner }}>
            {children}
        </AuthContext.Provider>
    );
};

export function useAuth() {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error("useAuth must be used within an AuthProvider");
    }
    return context;
}
