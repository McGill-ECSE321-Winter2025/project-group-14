import React, { useState, useEffect } from 'react';
import { useAuth } from '../../AuthContext';
import { UserManagementAPI } from '../../UserManagementAPI';
import AccountSettings from './AccountSettings';

import GameHistory from './GameHistory';


import '../../styles/layout.css';
import './Account.css';
import '../../styles/tabs.css';
import '../../styles/card.css';
import MyReviews from './MyReviews';
import {
    Box,
    CardMedia,
    CircularProgress,
    Tabs,
    Tab
  } from "@mui/material";



const Account = () => {
    const { user, isOwner } = useAuth();
    const [userDetails, setUserDetails] = useState(null);
    const [tabValue, setTabValue] = useState(0);

    const handleTabChange = (event, tab) => {
        setTabValue(tab);
    };

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

    return (
        <div>
            <Box sx={{
                width: '100%',
                mb: 3,
                '& .MuiTabs-indicator': { backgroundColor: 'black', height: '3px' },
                '& .MuiTab-root': {
                    color: '#666',
                    fontSize: '1rem',
                    textTransform: 'none',
                    fontWeight: 500,
                    padding: '12px 24px',
                    minWidth: 'unset',
                    '&.Mui-selected': { color: 'black', fontWeight: 600 },
                    '&:hover': { color: 'black', opacity: 1 }
                }
            }}>
                <Tabs value={tabValue} onChange={handleTabChange} centered variant="fullWidth">
                    <Tab label="My Account" />
                    <Tab label="My Reviews" />
                    <Tab label="Game History" />
                    
                </Tabs>
            </Box>

            <Box className="tab-content fade-in-on-scroll">

                {tabValue === 0 && <AccountSettings />}
                {tabValue === 1 && <MyReviews />}
                {tabValue === 2 && <GameHistory />}
                

            </Box>
        </div>
    );
};

export default Account;