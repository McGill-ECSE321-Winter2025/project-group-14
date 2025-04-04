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

const AccountSettings = () => {
    const { user } = useAuth();
    const [userInfo, setUserInfo] = useState(null);
    const [email, setEmail] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [oldPassword, setOldPassword] = useState('');
    const [updateStatus, setUpdateStatus] = useState('');
    const [infoLocked, setInfoLocked] = useState('');
    const [confirmDeleteAccountOpen, setConfirmDeleteAccountOpen] = useState(false);

    useEffect(() => {
        if (user?.userId) {
            UserManagementAPI.getUserDetails(user.userId).then(setUserInfo);
        }
    }, [user]);

    const handleUpdate = async () => {
        if (!oldPassword) return alert("Enter your current password to proceed.");

        const success = await UserManagementAPI.updateUser(
            user.userId,
            oldPassword,
            email || null,
            newPassword || null
        );

        if (success) {
            const updatedInfo = await UserManagementAPI.getUserDetails(user.userId);
            setUpdateStatus("Updated successfully.");
            setEmail('');
            setNewPassword('');
            setOldPassword('');
        } else {
            setUpdateStatus("Update failed.");
        }
    };

    const handleDeleteAccount = async () => {
        setConfirmDeleteAccountOpen(true);
    };

    const deleteAccountConfirmed = async () => {
        setConfirmDeleteAccountOpen(false)
        const success = await UserManagementAPI.deleteUser(user.userId);
        if (success) {
            sessionStorage.clear();
            window.location.href = "/";
        }
    };

    const deleteAccountCanceled = () => {
        setConfirmDeleteAccountOpen(false);
    }

    const InfoIcon = ({ id }) => (
        <button
            className="info-icon-container"
            onClick={() => setInfoLocked(infoLocked === id ? '' : id)}
            aria-label="Information"
        >
            <FiInfo className="info-icon" />
        </button>
    );

    const InfoText = ({ id, children }) => (
        infoLocked === id && (
            <div className="info-text">{children}</div>
        )
    );

    return (
        <div className="account-settings-container">
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

                <div className="settings-section">
                    <div className="section-header">
                        <div className="section-title">
                            <FiMail className="section-icon" />
                            <h3>Update Credentials</h3>
                        </div>
                        <InfoIcon id="emailPass" />
                    </div>
                    <InfoText id="emailPass">Change email or password. Current password required.</InfoText>

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
                            <label htmlFor="currentPassword">
                                <FiLock className="input-icon" />
                                Current Password
                            </label>
                            <input
                                id="currentPassword"
                                type="password"
                                placeholder="Enter current password"
                                required
                                value={oldPassword}
                                onChange={e => setOldPassword(e.target.value)}
                            />
                        </div>

                        <button className="update-button" onClick={handleUpdate}>
                            <FiCheck className="button-icon" />
                            Save Changes
                        </button>

                        {updateStatus && (
                            <p className={`status-message ${updateStatus.includes("success") ? "success" : "error"}`}>
                                {updateStatus}
                            </p>
                        )}
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
    );
};

export default AccountSettings;