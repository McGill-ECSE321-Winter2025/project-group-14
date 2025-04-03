import React, { useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthContext } from "../../AuthContext";
import "./Navbar.css";

function Navbar() {
    const { user, logout, isOwner } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        setTimeout(() => navigate("/"), 0);
    };

    return (
        <nav className="navbar">
            <div className="nav-left">
                {!user && <Link to="/">Home</Link>}
                {user && <Link to="/games" className="nav-item">Games</Link>}
                {user && <Link to="/events" className="nav-item">Events</Link>}
                {user && <Link to="/about" className="nav-item">About</Link>}
                
                {/* For Owners only */}
                {user && isOwner && (
                    <Link to="/received-requests" className="nav-item">
                        Borrowing Requests
                    </Link>
                )}

                {/* For Non-Owners (is_active = false) only */}
                {user && !isOwner && (
                    <Link to="/sent-requests" className="nav-item">
                        My Sent Requests
                    </Link>
                )}
            </div>

            <div className="nav-center">
                <img
                    src="https://cpmfiles1.com/christchurchsa.com/game_night_logo_web.png"
                    alt="Game Night Temporary Logo"
                    className="navbar-logo"
                />
            </div>

            <div className="nav-right">
                {user && isOwner && (
                    <Link to="/my-games" className="nav-item">
                        My Games
                    </Link>
                )}
                {user && <Link to="/my-events" className="nav-item">My Events</Link>}
                {user && <Link to="/account" className="nav-item">My Account</Link>}

                {user ? (
                    <button
                        onClick={handleLogout}
                        className="nav-item"
                        style={{ font: "inherit", fontWeight: "bold", background: "none", border: "none", cursor: "pointer" }}
                    >
                        Logout
                    </button>
                ) : (
                    <>
                        <Link to="/login" className="nav-item">Log In</Link>
                        <Link to="/signup" className="nav-item">Sign Up</Link>
                    </>
                )}
            </div>
        </nav>
    );
}

export default Navbar;
