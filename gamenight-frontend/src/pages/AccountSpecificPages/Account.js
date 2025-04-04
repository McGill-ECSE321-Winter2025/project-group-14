import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../../AuthContext';
import { UserManagementAPI } from '../../UserManagementAPI';
import AccountSettings from './AccountSettings';
import GameHistory from './GameHistory';
import MyEvents from './EventHistory';
import '../../styles/layout.css';
import './Account.css';
import '../../styles/tabs.css';
import '../../styles/card.css';
import Box from '../../components/ui/Box';
import MyReviews from './MyReviews';

const Account = () => {
    const { user, isOwner } = useAuth();
    const [activeTab, setActiveTab] = useState('settings');
    const [userDetails, setUserDetails] = useState(null);

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

    const placeholderImage = `https://i.pravatar.cc/150?u=${user?.userId || 'guest'}`;

    return (
        <Box>
            <h1 className="centered">My Account</h1>

            <div className="central-image-container fade-in-on-scroll">
                <div className="profile-container">
                    <img className="profile-image" src={placeholderImage} alt="profile" />
                    <div className="profile-details">
                        <h2>{userDetails?.name || 'Unknown User'}</h2>
                        <p>{userDetails?.email || 'Unknown Email'}</p>
                        <p>{isOwner ? 'Game Owner' : 'Player'}</p>
                    </div>
                </div>
            </div>

            <div className="tabs fade-in-on-scroll">
                <button className={`tab ${activeTab === 'settings' ? 'active' : ''}`} onClick={() => setActiveTab('settings')}>Settings</button>
                <button className={`tab ${activeTab === 'reviews' ? 'active' : ''}`} onClick={() => setActiveTab('reviews')}>My Reviews</button>
                <button className={`tab ${activeTab === 'games' ? 'active' : ''}`} onClick={() => setActiveTab('games')}>Game History</button>
                <button className={`tab ${activeTab === 'events' ? 'active' : ''}`} onClick={() => setActiveTab('events')}>Event History</button>
            </div>

            <Box className="tab-content fade-in-on-scroll">
                {activeTab === 'settings' && <AccountSettings />}
                {activeTab === 'reviews' && <MyReviews />}
                {activeTab === 'games' && <MyGames />}
                {activeTab === 'events' && <MyEvents />}
            </Box>
        </Box>
    );
};

export default Account;
