import React, { useState, useEffect } from 'react';
import { UserManagementAPI } from '../../UserManagementAPI';
import { useAuth } from '../../AuthContext';
import './AccountSettings.css';

const AccountSettings = () => {
    const { user, isOwner } = useAuth();
    const [userInfo, setUserInfo] = useState(null);
    const [email, setEmail] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [oldPassword, setOldPassword] = useState('');
    const [statusMessage, setStatusMessage] = useState('');
    const [infoLocked, setInfoLocked] = useState('');

    useEffect(() => {
        if (user?.userId) {
            UserManagementAPI.getUserDetails(user.userId).then(setUserInfo);
        }
    }, [user]);

    const handleUpdate = async () => {
        if (!oldPassword) return alert("Enter your current password to proceed.");

        const success = await UserManagementAPI.updateUser(user.userId, oldPassword, email || null, newPassword || null);

        if (success) {
            const updatedInfo = await UserManagementAPI.getUserDetails(user.userId);
            setUserInfo(updatedInfo);
            setStatusMessage("Updated successfully.");
            setEmail('');
            setNewPassword('');
            setOldPassword('');
        } else {
            setStatusMessage("Update failed.");
        }
    };

    const handleToggleRole = async () => {
        const success = await UserManagementAPI.toggleRole(user.userId);
        if (success) alert("Role toggled successfully.");
        else alert("Toggle failed.");
    };

    const handleDeleteAccount = async () => {
        if (window.confirm("Are you sure? This cannot be undone.")) {
            const success = await UserManagementAPI.deleteUser(user.userId);
            if (success) {
                sessionStorage.clear();
                window.location.href = "/";
            }
        }
    };

    const InfoIcon = ({ id }) => (
        <div
            className="info-icon-container"
            onClick={() => setInfoLocked(infoLocked === id ? '' : id)}
        >
            <span className="info-icon">ℹ</span>
        </div>
    );

    const InfoText = ({ id, children }) => (
        infoLocked === id && (
            <div className="info-text">{children}</div>
        )
    );

    return (
        <div className="account-settings-container">

            <div className="settings-card">
                <h2 className="centered">Profile Info</h2>
                <p><strong>Username:</strong> {userInfo?.name || "Unknown"}</p>
                <p><strong>Email:</strong> {userInfo?.email || "Unknown"}</p>
                <p><strong>Owner:</strong> {isOwner ? 'Yes' : 'No'}</p>
            </div>

            <div className="settings-card">
                <h2 className="centered">Update Email & Password</h2>
                <InfoIcon id="emailPass" />
                <InfoText id="emailPass">Change email or password. Current password required.</InfoText>
                <div className="auth-form">
                    <input type="email" placeholder="New Email" value={email} onChange={e => setEmail(e.target.value)} />
                    <input type="password" placeholder="New Password" value={newPassword} onChange={e => setNewPassword(e.target.value)} />
                    <input type="password" placeholder="Current Password" required value={oldPassword} onChange={e => setOldPassword(e.target.value)} />
                    <button onClick={handleUpdate}>Save Changes</button>
                    {statusMessage && <p className="centered">{statusMessage}</p>}
                </div>
            </div>

            <div className="settings-card">
                <h2 className="centered">Role Toggle</h2>
                <InfoIcon id="toggleRole" />
                <InfoText id="toggleRole">Switch between player and owner mode.</InfoText>
                <div className="auth-form">
                    <button onClick={handleToggleRole}>Toggle Role</button>
                </div>
            </div>

            <div className="settings-card">
                <h2 className="centered">Delete Account</h2>
                <InfoIcon id="delete" />
                <InfoText id="delete">Deletes your account permanently.</InfoText>
                <div className="auth-form">
                    <button className="delete-btn" onClick={handleDeleteAccount}>Delete Account</button>
                </div>
            </div>

        </div>
    );
};

export default AccountSettings;
