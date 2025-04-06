import React, { useState, useEffect } from 'react';
import { UserManagementAPI } from '../../UserManagementAPI';
import { useAuth } from '../../AuthContext';
import './AccountSettings.css';
import AccountRoleToggle from './AccountRoleToggle';
import { FiInfo, FiMail, FiLock, FiTrash2, FiUser, FiCheck } from 'react-icons/fi';
import {
    Typography,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle
} from "@mui/material";
import Button from './../../components/ui/Button'
import { useNavigate } from 'react-router-dom';
import { usePopup } from '../../components/PopupContext';

const AccountSettings = () => {
    const { user, logout, isOwner } = useAuth();
    const [userDetails, setUserDetails] = useState(null);
    const [userInfo, setUserInfo] = useState(null);
    const [email, setEmail] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [oldPassword, setOldPassword] = useState('');
    const [updateStatus, setUpdateStatus] = useState('');
    const [infoLocked, setInfoLocked] = useState('');
    const [confirmDeleteAccountOpen, setConfirmDeleteAccountOpen] = useState(false);
    const [emailSectionOpen, setEmailSectionOpen] = useState(true);
    const [passwordSectionOpen, setPasswordSectionOpen] = useState(false);
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

    const handleUpdateEmail = async () => {
        if (!oldPassword || !email) return alert("Provide current password and new email.");
        const success = await UserManagementAPI.updateUser(
            user.userId,
            oldPassword,
            email,
            oldPassword
        );

        if (success) {
            const updatedInfo = await UserManagementAPI.getUserDetails(user.userId);
            setUserDetails(updatedInfo);
            setUpdateStatus("Email updated successfully.");
            setEmail('');
            setOldPassword('');
        } else {
            setUpdateStatus("Email update failed.");
        }
    };

    const handleUpdatePassword = async () => {
        if (!oldPassword || !newPassword) return alert("Provide current and new password.");
        const currentEmail = userDetails?.email || userInfo?.email;

        const success = await UserManagementAPI.updateUser(
            user.userId,
            oldPassword,
            currentEmail,
            newPassword
        );

        if (success) {
            setUpdateStatus("Password updated successfully.");
            setNewPassword('');
            setOldPassword('');
        } else {
            setUpdateStatus("Password update failed.");
        }
    };

    const handleDeleteAccount = async () => {
        setConfirmDeleteAccountOpen(true);
    };

    const deleteAccountConfirmed = async () => {
        setConfirmDeleteAccountOpen(false);
        const success = await UserManagementAPI.deleteUser(user.userId);
        if (success) {
            logout();
            setTimeout(() => navigate("/"), 0);
        } else {
            showPopup("Deleting account failed.");
        }
    };

    const deleteAccountCanceled = () => {
        setConfirmDeleteAccountOpen(false);
    };

    const InfoIcon = ({ id }) => (
        <button
            className="info-icon-container"
            onClick={() => setInfoLocked(infoLocked === id ? '' : id)}
            aria-label="Information"
        >
            <FiInfo className="info-icon" />
        </button>
    );

    const InfoText = ({ id, children }) =>
        infoLocked === id ? <div className="info-text">{children}</div> : null;

    return (
        <div className='account-settings-container'>
            <div className='account-settings-item '>
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
                        <InfoIcon id="delete" />
                    </div>
                    <InfoText id="delete">Deletes your account permanently.</InfoText>
                    <button className="delete-button" onClick={handleDeleteAccount}>
                        <FiTrash2 className="button-icon" />
                        Delete Account
                    </button>
                </div>
            </div>

            <div className='account-settings-item '>
                <div className="settings-card">
                    <div className="card-header">
                        <div className="header-title">
                            <FiUser className="header-icon" />
                            <h2>Account Settings</h2>
                        </div>
                        <div className="header-actions">
                            <AccountRoleToggle userId={user.userId} />
                            <InfoIcon id="toggleRole" />
                        </div>
                    </div>
                    <InfoText id="toggleRole">Switch between player and owner mode.</InfoText>

                    {/* Update Email */}
                    <div className="settings-section">
                        <div
                            className="section-header collapsible-header"
                            onClick={() => setEmailSectionOpen(!emailSectionOpen)}
                        >
                            <div className="section-title">
                                <FiMail className="section-icon" />
                                <h3>Update Email</h3>
                            </div>
                            <FiInfo className={`toggle-icon ${emailSectionOpen ? 'open' : ''}`} />
                        </div>
                        <InfoText id="emailUpdate">Change your email. Requires current password.</InfoText>

                        {emailSectionOpen && (
                            <div className="auth-form">
                                <div className="form-group">
                                    <label htmlFor="email">
                                        <FiMail className="input-icon" />
                                        New Email
                                    </label>
                                    <input
                                        id="email"
                                        type="email"
                                        placeholder="Enter new email address"
                                        value={email}
                                        onChange={e => setEmail(e.target.value)}
                                    />
                                </div>
                                <div className="form-group">
                                    <label htmlFor="currentPasswordEmail">
                                        <FiLock className="input-icon" />
                                        Current Password
                                    </label>
                                    <input
                                        id="currentPasswordEmail"
                                        type="password"
                                        placeholder="Enter current password"
                                        value={oldPassword}
                                        onChange={e => setOldPassword(e.target.value)}
                                    />
                                </div>
                                <button className="update-button" onClick={handleUpdateEmail}>
                                    <FiCheck className="button-icon" />
                                    Update Email
                                </button>
                            </div>
                        )}
                    </div>

                    {/* Update Password */}
                    <div className="settings-section">
                        <div
                            className="section-header collapsible-header"
                            onClick={() => setPasswordSectionOpen(!passwordSectionOpen)}
                        >
                            <div className="section-title">
                                <FiLock className="section-icon" />
                                <h3>Update Password</h3>
                            </div>
                            <FiInfo className={`toggle-icon ${passwordSectionOpen ? 'open' : ''}`} />
                        </div>
                        <InfoText id="passwordUpdate">Change your password. Requires current password.</InfoText>

                        {passwordSectionOpen && (
                            <div className="auth-form">
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
