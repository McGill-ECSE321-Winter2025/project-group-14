import React from "react";
import { Link } from "react-router-dom";

function Layout({ children, isAuthenticated }) {
    return (
        <div>
            {/* NAVIGATION BAR */}
            <nav style={{ display: "flex", justifyContent: "space-between", padding: "10px", background: "#222", color: "white" }}>
                <div>
                    <Link to="/" style={{ color: "white", marginRight: "20px" }}>Home</Link>
                    <Link to="/games" style={{ color: "white", marginRight: "20px" }}>My Games</Link>
                    <Link to="/events" style={{ color: "white", marginRight: "20px" }}>My Events</Link>
                </div>
                <div>
                    {isAuthenticated ? (
                        <Link to="/account" style={{ color: "white" }}>My Account</Link>
                    ) : (
                        <>
                            <Link to="/login" style={{ color: "white", marginRight: "10px" }}>Log In</Link>
                            <Link to="/signup" style={{ color: "white" }}>Sign Up</Link>
                        </>
                    )}
                </div>
            </nav>

            {/* MAIN CONTENT */}
            <main style={{ padding: "20px" }}>
                {children}
            </main>
        </div>
    );
}

export default Layout;
