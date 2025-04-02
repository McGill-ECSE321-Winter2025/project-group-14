import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../../AuthContext';
import { UserManagementAPI } from '../../UserManagementAPI';
import AccountSettings from './AccountSettings';
import MyGames from './MyGames';
import MyEvents from './MyEvents';
import '../../styles/layout.css';
import './Account.css';
import '../../styles/tabs.css';
import '../../styles/card.css';
import Box from '../../components/ui/Box';

const Account = () => {
    const { user } = useContext(AuthContext);
    const [activeTab, setActiveTab] = useState('settings');
    const [playerId, setPlayerId] = useState(null);
    const [ownerName, setOwnerName] = useState(null);
    const [userInfo, setUserInfo] = useState(null);

    useEffect(() => {
        if (user?.userId) {
            UserManagementAPI.getPlayerId(user.userId).then(setPlayerId);
            UserManagementAPI.getGameOwnerName(user.userId).then(setOwnerName);
            UserManagementAPI.getUserDetails(user.userId).then(setUserInfo);
        }

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

            {/* ✅ PROFILE */}
            <div className="central-image-container fade-in-on-scroll">
                <div className="profile-container">
                    <img className="profile-image" src={placeholderImage} alt="profile" />
                    <div className="profile-details">
                        <h2>{userInfo?.name || 'Unknown User'}</h2>
                        <p>{userInfo?.email || 'Unknown Email'}</p>
                        <p>{ownerName ? 'Owner' : 'Not an Owner'}</p>
                    </div>
                </div>
            </div>

            {/* ✅ TABS */}
            <div className="tabs fade-in-on-scroll">
                <button className={`tab ${activeTab === 'settings' ? 'active' : ''}`} onClick={() => setActiveTab('settings')}>Settings</button>
                <button className={`tab ${activeTab === 'reviews' ? 'active' : ''}`} onClick={() => setActiveTab('reviews')}>Past Reviews</button>
                <button className={`tab ${activeTab === 'games' ? 'active' : ''}`} onClick={() => setActiveTab('games')}>Game History</button>
                <button className={`tab ${activeTab === 'events' ? 'active' : ''}`} onClick={() => setActiveTab('events')}>Event History</button>
            </div>

            {/* ✅ TAB CONTENT */}
            <Box className="tab-content fade-in-on-scroll">
                {activeTab === 'settings' && <AccountSettings user={user} />}
                {activeTab === 'reviews' && <p>Your submitted reviews will appear here.</p>}
                {activeTab === 'games' && <MyGames />}
                {activeTab === 'events' && <MyEvents />}
            </Box>
        </Box>
    );
};

export default Account;
