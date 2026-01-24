import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import AdminDashboard from './components/AdminDashboard';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/admin" element={<AdminDashboard />} />
          <Route path="/" element={<div className="p-10 text-center text-2xl">Welcome to VPN Service</div>} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
