import React, { useContext } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../AuthContext"; // Import AuthContext
import "./Navbar.css";

function Navbar() {
    const { user, logout } = useContext(AuthContext); // Get user state

    return (
        <nav>
            <div className="nav-left">
                {!user && <Link to="/">Home</Link>} {/* Show Home only if not logged in */}
                {user && <Link to="/my-games">My Games</Link>}
                {user && <Link to="/my-events">My Events</Link>}
            </div>

            <div className="nav-right">
                {user && <Link to="/account">My Account</Link>}
                {user ? (
                    <button onClick={logout}>Logout</button>
                ) : (
                    <>
                        <Link to="/login">Log In</Link>
                        <Link to="/signup">Sign Up</Link>
                    </>
                )}
            </div>
        </nav>
    );
}

export default Navbar;
