import React, { useState, useContext } from "react";
import { AuthContext } from "../AuthContext";
import { useNavigate } from "react-router-dom";
import Button from "../components/Button";
import Box from "../components/Box";

function Login() {
    const { login } = useContext(AuthContext);
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const handleLogin = async (e) => {
        e.preventDefault();
        setError("");

        const user = await login(email, password);
        if (user) {
            navigate("/games");
        } else {
            setError("Invalid email or password.");
        }
    };

    return (
        <div className="container">
            <Box>
                <h1 className="centered">Login</h1>
                {error && <p className="error-text">{error}</p>} {/* Error message inside UI */}
                <form onSubmit={handleLogin} className="auth-form">
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
                    <Button type="success">Log In</Button>
                </form>
                <p className="centered">Don't have an account? <a href="/signup">Sign up</a></p>
            </Box>
        </div>
    );
}

export default Login;
