import React, { createContext, useState, useEffect, useContext } from "react";
import { UserManagementAPI } from "./UserManagementAPI";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isOwner, setIsOwner] = useState(false);

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

    const login = async (email, password) => {
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
        }
        return userData;
    };

    const logout = () => {
        sessionStorage.removeItem("user");
        setUser(null);
        setIsOwner(false);
    };

    return (
        <AuthContext.Provider value={{ user, setUser, loading, login, logout, isOwner }}>
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
