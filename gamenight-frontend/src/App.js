import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./AuthContext";
import Navbar from "./components/Navbar";
import ProtectedRoute from "./components/ProtectedRoute";

// Pages
import Home from "./pages/Home";
import Login from "./pages/Login";
import SignUp from "./pages/SignUp";
import Games from "./pages/GamePage";
import MyGames from "./pages/MyGames";
import MyEvents from "./pages/MyEvents";
import Events from "./pages/Events";
import Account from "./pages/Account";
import About from "./pages/About";
import SentRequests from "./pages/SentRequests";
import ActiveRentalsPage from "./pages/ActiveRentalsPage";
import ReceivedBorrowingRequests from "./pages/ReceivedBorrowingRequests";
import AddGame from "./pages/AddGame";

function App() {
  return (
    <Router>
      <AuthProvider>
        <Navbar />
        <Routes>

          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
         
          <Route path="/sent-requests" element={<SentRequests />} /></ProtectedRoute>} />
          <Route path="/active-rentals" element={<ActiveRentalsPage />} /></ProtectedRoute>} />
          <Route path="/games" element={<ProtectedRoute><Games /></ProtectedRoute>} />
          <Route path="/my-games" element={<ProtectedRoute><MyGames /></ProtectedRoute>} />
          <Route path="/my-events" element={<ProtectedRoute><MyEvents /></ProtectedRoute>} />
          <Route path="/account" element={<ProtectedRoute><Account /></ProtectedRoute>} />
          <Route path="/events" element={<ProtectedRoute><Events /></ProtectedRoute>} />
          <Route path="/about" element={<ProtectedRoute><About /></ProtectedRoute>} />
          <Route path="/add-game" element={<ProtectedRoute><AddGame /></ProtectedRoute>} />
           <Route path="/received-requests" element={<ReceivedBorrowingRequests />} /></ProtectedRoute>} />                             

          <Route path="*" element={<Navigate to="/" />} />

        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;
