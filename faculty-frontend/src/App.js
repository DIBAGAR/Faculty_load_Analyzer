import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import FacultyDashboard from "./pages/FacultyDashboard"; // ✅ Updated path

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/faculty/dashboard" element={<FacultyDashboard />} />
      </Routes>
    </Router>
  );
}

export default App;
