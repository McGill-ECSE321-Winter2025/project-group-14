import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./AuthContext";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./pages/Login";
import SignUp from "./pages/SignUp";
import MyGames from "./pages/MyGames";
import MyEvents from "./pages/MyEvents";
import Account from "./pages/Account";

function App() {
  return (
    <Router>
      <AuthProvider>
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/my-games" element={<MyGames />} />
          <Route path="/my-events" element={<MyEvents />} />
          <Route path="/account" element={<Account />} />
        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;
