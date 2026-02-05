import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import AdminDashboard from './pages/AdminDashboard';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/admin" element={<AdminDashboard />} />
          <Route path="/" element={<div>Welcome to DataGuard VPN</div>} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
