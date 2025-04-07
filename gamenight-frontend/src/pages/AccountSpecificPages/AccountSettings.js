import React, { useState, useEffect } from 'react';
import { UserManagementAPI } from '../../UserManagementAPI';
import { useAuth } from '../../AuthContext';
import './AccountSettings.css';
import AccountRoleToggle from './AccountRoleToggle';
import { FiLock, FiTrash2, FiUser, FiCheck } from 'react-icons/fi';
import {
    Typography,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle
} from "@mui/material";
import Button from './../../components/ui/Button';
import { useNavigate } from 'react-router-dom';
import { usePopup } from '../../components/PopupContext';

const AccountSettings = () => {
    const { user, logout, isOwner } = useAuth();
    const [userDetails, setUserDetails] = useState(null);
    const [userInfo, setUserInfo] = useState(null);
    const [username, setUsername] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [oldPassword, setOldPassword] = useState('');
    const [updateStatus, setUpdateStatus] = useState('');
    const [confirmDeleteAccountOpen, setConfirmDeleteAccountOpen] = useState(false);
    const [openSection, setOpenSection] = useState('username');

    const placeholderImage = `https://i.pravatar.cc/150?u=${user?.userId || 'guest'}`;
    const navigate = useNavigate();
    const { showPopup } = usePopup();

    useEffect(() => {
        if (user?.userId) {
            UserManagementAPI.getUserDetails(user.userId).then(setUserInfo);
        }
    }, [user]);

    useEffect(() => {
        if (!user?.userId) return;

        const fetchDetails = async () => {
            const details = await UserManagementAPI.getUserDetails(user.userId);
            setUserDetails(details);
        };
        fetchDetails();

        const handleFadeIn = () => {
            document.querySelectorAll('.fade-in-on-scroll').forEach(section => {
                const rect = section.getBoundingClientRect();
                if (rect.top < window.innerHeight - 50) {
                    section.classList.add('visible');
                }
            });
        };

        window.addEventListener('scroll', handleFadeIn);
        window.addEventListener('resize', handleFadeIn);
        handleFadeIn();

        return () => {
            window.removeEventListener('scroll', handleFadeIn);
            window.removeEventListener('resize', handleFadeIn);
        };
    }, [user]);

    const toggleSection = (section) => {
        setOpenSection(prev => (prev === section ? '' : section));
    };

    const handleUpdateUsername = async () => {
        if (!username) return showPopup("Please enter a new username.");

        try {
            const success = await UserManagementAPI.updateUsername(user.userId, username);
            if (!success) throw new Error("Username update failed.");
            setUsername('');
            const updated = await UserManagementAPI.getUserDetails(user.userId);
            setUserDetails(updated);
            setUpdateStatus("Username updated successfully.");
        } catch (err) {
            showPopup(err.message || "Username update failed.");
        }
    };

    const handleUpdatePassword = async () => {
        if (!newPassword || !oldPassword)
            return showPopup("Please fill in both new and current passwords.");

        try {
            const currentEmail = userDetails?.email || userInfo?.email;
            const success = await UserManagementAPI.updateUser(user.userId, oldPassword, currentEmail, newPassword);
            if (!success) throw new Error("Password update failed.");
            setNewPassword('');
            setOldPassword('');
            setUpdateStatus("Password updated successfully.");
        } catch (err) {
            showPopup(err.message || "Password update failed.");
        }
    };

    const handleDeleteAccount = () => {
        setConfirmDeleteAccountOpen(true);
    };

    const deleteAccountConfirmed = async () => {
        setConfirmDeleteAccountOpen(false);
        try {
            const success = await UserManagementAPI.deleteUser(user.userId);
            if (success) {
                logout();
                navigate("/");
            } else {
                showPopup("Deleting account failed.");
            }
        } catch (err) {
            showPopup(err.message || "Deleting account failed.");
        }
    };

    const deleteAccountCanceled = () => {
        setConfirmDeleteAccountOpen(false);
    };

    return (
        <div className='account-settings-container'>
            <div className='account-settings-item'>
                <div className="profile-container">
                    <img className="profile-image" src={placeholderImage} alt="profile" />
                    <div className="profile-details">
                        <h2>{userDetails?.name || 'Unknown User'}</h2>
                        <p>{userDetails?.email || 'Unknown Email'}</p>
                        <p>{isOwner ? 'Game Owner' : 'Player'}</p>
                    </div>
                </div>

                <div className="settings-section danger-section">
                    <div className="section-header">
                        <div className="section-title">
                            <FiTrash2 className="section-icon" />
                            <h3>Delete Account</h3>
                        </div>
                    </div>
                    <button className="delete-button" onClick={handleDeleteAccount}>
                        <FiTrash2 className="button-icon" />
                        Delete Account
                    </button>
                </div>
            </div>

            <div className='account-settings-item'>
                <div className="settings-card">
                    <div className="card-header">
                        <div className="header-title">
                            <FiUser className="header-icon" />
                            <h2>Account Settings</h2>
                        </div>
                        <div className="header-actions">
                            <AccountRoleToggle userId={user.userId} />
                        </div>
                    </div>

                    {/* Update Username */}
                    <div className="settings-section">
                        <div
                            className="section-header collapsible-header"
                            onClick={() => toggleSection('username')}
                        >
                            <div className="section-title">
                                <FiUser className="section-icon" />
                                <h3>Update Username</h3>
                            </div>
                        </div>

                        {openSection === 'username' && (
                            <div className="auth-form">
                                <div className="form-group">
                                    <label htmlFor="username">
                                        <FiUser className="input-icon" />
                                        New Username
                                    </label>
                                    <input
                                        id="username"
                                        type="text"
                                        placeholder="Enter new username"
                                        value={username}
                                        onChange={e => setUsername(e.target.value)}
                                    />
                                </div>
                                <button className="update-button" onClick={handleUpdateUsername}>
                                    <FiCheck className="button-icon" />
                                    Update Username
                                </button>
                            </div>
                        )}
                    </div>

                    {/* Update Password */}
                    <div className="settings-section">
                        <div
                            className="section-header collapsible-header"
                            onClick={() => toggleSection('password')}
                        >
                            <div className="section-title">
                                <FiLock className="section-icon" />
                                <h3>Update Password</h3>
                            </div>
                        </div>

                        {openSection === 'password' && (
                            <div className="auth-form">
                                <div className="form-group">
                                    <label htmlFor="currentPasswordPass">
                                        <FiLock className="input-icon" />
                                        Current Password
                                    </label>
                                    <input
                                        id="currentPasswordPass"
                                        type="password"
                                        placeholder="Enter current password"
                                        value={oldPassword}
                                        onChange={e => setOldPassword(e.target.value)}
                                    />
                                </div>
                                <div className="form-group">
                                    <label htmlFor="newPassword">
                                        <FiLock className="input-icon" />
                                        New Password
                                    </label>
                                    <input
                                        id="newPassword"
                                        type="password"
                                        placeholder="Enter new password"
                                        value={newPassword}
                                        onChange={e => setNewPassword(e.target.value)}
                                    />
                                </div>
                                <button className="update-button" onClick={handleUpdatePassword}>
                                    <FiCheck className="button-icon" />
                                    Update Password
                                </button>
                            </div>
                        )}

                    </div>

                    {updateStatus && (
                        <p className={`status-message ${updateStatus.includes("success") ? "success" : "error"}`}>
                            {updateStatus}
                        </p>
                    )}
                </div>

                <Dialog open={confirmDeleteAccountOpen}>
                    <DialogTitle>Warning</DialogTitle>
                    <DialogContent>
                        <Typography>Do you really want to delete your account? This action cannot be undone.</Typography>
                    </DialogContent>
                    <DialogActions>
                        <Button type="success" onClick={deleteAccountConfirmed}>
                            Delete account
                        </Button>
                        <Button type="danger" onClick={deleteAccountCanceled}>
                            Cancel
                        </Button>
                    </DialogActions>
                </Dialog>
            </div>
        </div>
    );
};

export default AccountSettings;
