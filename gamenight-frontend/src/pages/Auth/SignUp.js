import React, { useState, useContext } from "react";
import { AuthContext } from "../../AuthContext";
import { UserManagementAPI } from "../../UserManagementAPI";
import Button from '../../components/ui/Button';
import Box from '../../components/ui/Box';


function SignUp() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [name, setName] = useState("");
    const [error, setError] = useState(null);

    const { login } = useContext(AuthContext);
    const handleSignUp = async (e) => {
        e.preventDefault();
        setError(null);

        const success = await UserManagementAPI.registerUser(email, password, name);

        if (success) {
            const userData = await login(email, password);
            if (!userData) {
                setError("Signed up but failed to log in.");
            }
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
