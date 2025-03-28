import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import GamePage from "./pages/GamePage";
import GameDetailsPage from "./pages/GameDetailsPage";
import { AuthProvider } from "./AuthContext";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./pages/Login";
import SignUp from "./pages/SignUp";
import MyGames from "./pages/MyGames";
import MyEvents from "./pages/MyEvents";
import Events from "./pages/Events";
import Account from "./pages/Account";
import About from "./pages/About";

function App() {
  return (
    <Router>
      <AuthProvider>
        <Navbar />
        <Routes>
          <Route path="/games" element={<GamePage/>} />
          <Route path="/games/:id" element={<GameDetailsPage/>} />
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/my-games" element={<MyGames />} />
          <Route path="/my-events" element={<MyEvents />} />
          <Route path="/account" element={<Account />} />
          <Route path="/events" element={<Events />} />
          <Route path="/about" element={<About />} />

        </Routes>
      </AuthProvider>
    </Router>
  );
}

export default App;
