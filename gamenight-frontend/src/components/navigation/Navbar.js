import React, { useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthContext } from "../../AuthContext"; // Assuming path is correct
import "./Navbar.css"; // Assuming path is correct

function Navbar() {
    const { user, logout, isOwner } = useContext(AuthContext); // Get isOwner state
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/");
    };

    return (
        <nav className="navbar">
            <div className="nav-left">
                {!user && <Link to="/">Home</Link>}
                {user && <Link to="/games">Games</Link>}
                {user && <Link to="/events">Events</Link>}
                {user && <Link to="/about">About</Link>}
                {user && isOwner && <Link to="/received-requests">Borrowing Requests</Link>}

                {/* Show "My Sent Requests" only if logged in AND NOT an owner */}
                {/* --- TEXT CHANGED HERE --- */}
                {user && !isOwner && <Link to="/sent-requests">My Sent Requests</Link> }
            </div>

            <div className="nav-center">
                <Link to="/">
                <img
                    src="https://cpmfiles1.com/christchurchsa.com/game_night_logo_web.png"
                    alt="Game Night Temporary Logo"
                    className="navbar-logo"
                />
                </Link>
            </div>

            <div className="nav-right">
                {user && isOwner && <Link to="/my-games">My Games</Link>}
                {user && <Link to="/my-events">My Events</Link>}
                {user && <Link to="/account">My Account</Link>}

                {user ? (
                    <Link to="#" onClick={handleLogout}>Logout</Link>
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