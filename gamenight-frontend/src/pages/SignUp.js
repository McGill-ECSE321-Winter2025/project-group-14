import React, { useState } from "react";
import { UserManagementAPI } from "../UserManagementAPI";
import { useNavigate } from "react-router-dom";

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
        <div>
            <h1>Sign Up</h1>
            {error && <p style={{ color: "red" }}>{error}</p>}
            <form onSubmit={handleSignUp}>
                <input
                    type="text"
                    placeholder="User Name"
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
                <button type="submit">Sign Up</button>
            </form>
            <p>Already have an account? <a href="/login">Log in</a></p>
        </div>
    );
}

export default SignUp;
