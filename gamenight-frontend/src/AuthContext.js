import React, { createContext, useState, useEffect, useContext } from "react";
import { UserManagementAPI } from "./UserManagementAPI";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

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
        }
        return userData;
    };


    const logout = () => {
        sessionStorage.removeItem("user");
        setUser(null);
    };


    return (
        <AuthContext.Provider value={{ user, setUser, loading, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
export function useAuth() {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}
