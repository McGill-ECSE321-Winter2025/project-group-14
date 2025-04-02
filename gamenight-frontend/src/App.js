import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./AuthContext";

// Components
import Navbar from "./components/navigation/Navbar";
import ProtectedRoute from "./components/navigation/ProtectedRoute";

// Pages

import Home from "./pages/Home/Home";
import Login from "./pages/Auth/Login";
import SignUp from "./pages/Auth/SignUp";
import GamePage from "./pages/Other/GamePage";
import MyGamesPage from "./pages/AccountSpecificPages/MyGames.js";
import MyEvents from "./pages/AccountSpecificPages/MyEvents";
import Events from "./pages/Other/Events";
import Account from "./pages/AccountSpecificPages/Account";
import About from "./pages/Other/About";
import ReceivedBorrowingRequests from "./pages/AccountSpecificPages/ReceivedBorrowingRequests";
import AddGame from "./pages/Other/AddGame";
import GameDetailsPage from "./pages/GameDetails/GameDetailsPage";


function App() {
  return (
    <Router>
      <AuthProvider>
        <Navbar />
        <Routes>

          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />

          <Route path="/games" element={<ProtectedRoute><GamePage /></ProtectedRoute>} />
          <Route path="/games/:id" element={<ProtectedRoute><GameDetailsPage /></ProtectedRoute>} />
          <Route path="/my-games" element={<ProtectedRoute><MyGamesPage /></ProtectedRoute>} />
          <Route path="/my-events" element={<ProtectedRoute><MyEvents /></ProtectedRoute>} />
          <Route path="/account" element={<ProtectedRoute><Account /></ProtectedRoute>} />
          <Route path="/events" element={<ProtectedRoute><Events /></ProtectedRoute>} />
          <Route path="/about" element={<ProtectedRoute><About /></ProtectedRoute>} />
          <Route path="/add-game" element={<ProtectedRoute><AddGame /></ProtectedRoute>} />
          <Route path="/received-requests" element={<ProtectedRoute><ReceivedBorrowingRequests /></ProtectedRoute>} />
          <Route path="/games/:id" element={<ProtectedRoute><GameDetailsPage /></ProtectedRoute>} />

          {/*<Route path="*" element={<Navigate to="/" />} />*/}

        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;
