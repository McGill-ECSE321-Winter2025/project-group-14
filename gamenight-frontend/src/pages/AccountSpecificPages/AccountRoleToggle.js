import { useState, useEffect } from "react";
import "./AccountRoleToggle.css"; // Import your styles
import { UserManagementAPI } from '../../UserManagementAPI';
import { useAuth } from '../../AuthContext';


export default function ToggleButton() {
    const { user, isOwner, refreshIsOwner } = useAuth();

    const handleToggleRole = async () => {
        const success = await UserManagementAPI.toggleRole(user.userId);
        if (success) {
            await refreshIsOwner();
        } else {
            alert("Toggle failed.");
        }
    }

    return (
        <div className="toggle-container">
            <span className={`label ${!isOwner ? "active" : ""}`}>Player</span>

            <label className="switch">
                <input
                    type="checkbox"
                    checked={isOwner}
                    onChange={() => handleToggleRole()}
                    aria-label="Toggle switch"
                />
                <span className="slider"></span>
            </label>

            <span className={`label ${isOwner ? "active" : ""}`}>Game owner</span>
        </div>
    );
}
