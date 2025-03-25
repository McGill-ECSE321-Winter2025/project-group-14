import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import GamePage from "./pages/GamePage";
import GameDetailsPage from "./pages/GameDetailsPage";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/games" element={<GamePage/>} />
        <Route path="/games/:id" element={<GameDetailsPage/>} />
      </Routes>
    </Router>
  );
}

export default App;
