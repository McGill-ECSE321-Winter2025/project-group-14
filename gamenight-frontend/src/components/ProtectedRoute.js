import React, { useContext } from "react";
import { AuthContext } from "../AuthContext";
import { Navigate } from "react-router-dom";

const ProtectedRoute = ({ children }) => {
    const { user, loading } = useContext(AuthContext); // ✨ added loading

    if (loading) return null; // ✨ just wait silently (or show "Loading...")

    if (!user) return <Navigate to="/login" />;

    return children;
};

export default ProtectedRoute;
