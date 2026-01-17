import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import AdminDashboard from './admin/dashboard/AdminDashboard';

const Home = () => (
    <div style={{ textAlign: 'center', marginTop: '50px' }}>
        <h1>DataGuard VPN</h1>
        <p>Fast, Secure, and Anonymous.</p>
        <Link to="/admin">Admin Panel</Link>
    </div>
);

const App = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/admin" element={<AdminDashboard />} />
            </Routes>
        </Router>
    );
};

export default App;
