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
                {user && <Link to="/games">Games</Link>}
                {user && <Link to="/events">Events</Link>}
                {user && <Link to="/about">About</Link>}
                {user && isOwner && <Link to="/received-requests">Borrowing Requests</Link>}
            </div>

            <div className="nav-center">
                <img
                    src="https://cpmfiles1.com/christchurchsa.com/game_night_logo_web.png"
                    alt="Game Night Temporary Logo"
                    className="navbar-logo"
                />
            </div>

            <div className="nav-right">
                {user && isOwner && <Link to="/my-games">My Games</Link>}
                {user && <Link to="/my-events">My Events</Link>}
                {user && <Link to="/account">My Account</Link>}

                {user ? (
                    <button
                        onClick={handleLogout}
                        style={{
                            font: 'inherit',
                            fontWeight: 'bold'
                        }}
                    >
                        Logout
                    </button>
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
