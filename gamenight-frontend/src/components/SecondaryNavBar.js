import React from "react";
import { Link } from "react-router-dom";

function SecondaryNavBar() {
    return (
        <div className="secondary-navbar">
            <Link to="/sent-requests" className="nav-item">Sent Borrowing Requests</Link>
            <Link to="/active-rentals" className="nav-item">Active Rentals</Link>
        </div>
    );
}

export default SecondaryNavBar;
