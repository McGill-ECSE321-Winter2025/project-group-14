import React, { useContext } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../../AuthContext";
import Button from "../../components/ui/Button";
import Box from "../../components/ui/Box";
import '../../pages/AccountSpecificPages/Account.css';


function Account() {
    const { user, logout } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        setTimeout(() => navigate("/"), 0); // ✅ ensures clean redirect
    };

    return (
        <Box dark>
            <div className="profile-container">
                <img className="profile-picture" src="https://i.pinimg.com/736x/09/81/88/098188d5e282211d513442f31cfd4ea8.jpg" alt="Profile" />
                <h2 className="profile-name">{user?.name || "@shrkek123"}</h2>
                <p>Email: {user?.email || "Not Connected to backend yet"}</p>

                <div className="profile-stats">
                    <Box>
                        <h3>2</h3>
                        <p>Friends</p>
                    </Box>
                    <Box>
                        <h3>5</h3>
                        <p>Games Played</p>
                    </Box>
                    <Box>
                        <h3>1</h3>
                        <p>Games Created</p>
                    </Box>
                </div>

                <Button seamless>Edit Profile</Button>
                <Button type="danger" onClick={handleLogout}>Logout</Button>
            </div>
        </Box>
    );
}

export default Account;
