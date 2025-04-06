import React, { useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthContext } from "../../AuthContext"; 
import "./Navbar.css"; 

function Navbar() {
    const { user, logout, isOwner } = useContext(AuthContext); 
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
                
  
                {user && <Link to="/sent-requests">My Borrowing Requests</Link> }
             
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
                {user && <Link to="/my-rentals">My Rentals</Link>}
                {user && isOwner && <Link to="/my-games">My Collection</Link>}
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