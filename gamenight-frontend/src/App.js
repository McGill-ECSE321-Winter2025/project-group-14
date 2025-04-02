import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./AuthContext";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./pages/Login";
import SignUp from "./pages/SignUp";
import Games from "./pages/Games";
import MyGames from "./pages/MyGames";
import MyEvents from "./pages/MyEvents";
import Events from "./pages/Events";
import Account from "./pages/Account";
import About from "./pages/About";
import SentRequests from "./pages/SentRequests";
import UpdatedStatusPage from "./pages/UpdatedStatusPage";

function App() {
  return (
    <Router>
      <AuthProvider>
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/games" element={<Games />} />
          <Route path="/my-games" element={<MyGames />} />
          <Route path="/my-events" element={<MyEvents />} />
          <Route path="/account" element={<Account />} />
          <Route path="/events" element={<Events />} />
          <Route path="/about" element={<About />} />
          <Route path="/sent-requests" element={<SentRequests />} />
          <Route path="/active-rentals" element={<UpdatedStatusPage />} />
        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;
