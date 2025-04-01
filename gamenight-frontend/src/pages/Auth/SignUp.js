import React, { useState, useContext } from "react";
import { AuthContext } from "../../AuthContext";
import { UserManagementAPI } from "../../UserManagementAPI";
import { useNavigate } from "react-router-dom"; // Import for navigation
import Button from '../../components/ui/Button';
import Box from '../../components/ui/Box';
import './SignUp.css';

function SignUp() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState(null);
  const { login } = useContext(AuthContext);
  const navigate = useNavigate(); // Hook for navigation

  const handleSignUp = async (e) => {
    e.preventDefault();
    setError(null);

    const success = await UserManagementAPI.registerUser(email, password, name);

    if (success) {
      // Option 1: Attempt to log in automatically
      const userData = await login(email, password);

      if (!userData) {
        setError("Signed up but failed to log in.");
      } else {
        // If auto-login is successful, redirect to home or dashboard
        navigate("/dashboard"); // Or wherever you want logged-in users to go
      }

      // Option 2: Redirect to login page regardless of auto-login attempt
      // Just redirect to login page with a success message
      navigate("/login", {
        state: { message: "Account created successfully. Please log in." }
      });
    } else {
      setError("Email already in use or invalid input.");
    }
  };

  return (
    <div className="container-signup">
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