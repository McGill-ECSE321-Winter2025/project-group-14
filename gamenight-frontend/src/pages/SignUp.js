import React, { useState } from "react";
import { UserManagementAPI } from "../UserManagementAPI";
import { useNavigate } from "react-router-dom";
import Button from "../components/Button";
import Box from "../components/Box";

function SignUp() {
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [error, setError] = useState(null);

    const handleSignUp = async (e) => {
        e.preventDefault();
        setError(null);

        const success = await UserManagementAPI.registerUser(email, password, name);

        if (success) {
            navigate("/login"); // Redirect to Login after successful signup
        } else {
            setError("Email already in use or invalid input.");
        }
    };


    return (
        <div className="container">
            <Box>
                <h1 className="centered">Sign Up</h1>
                {error && <p className="error-text">{error}</p>}
                <form onSubmit={handleSignUp} className="auth-form">
                    <input
                        type="text"
                        placeholder="Name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                    />
                    <input
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <Button type="success">Sign Up</Button>
                </form>
                <p className="centered">
                    Already have an account? <a href="/login">Log in</a>
                </p>
            </Box>
        </div>
    );

}

export default SignUp;
