import React from 'react';
import { Link } from 'react-router-dom';

const Home = () => {
  return (
    <div style={{ fontFamily: 'Arial, sans-serif', textAlign: 'center', padding: '50px' }}>
      <h1>Welcome to DataGuard VPN</h1>
      <p>Secure, Fast, and Private Internet Access.</p>

      <div style={{ marginTop: '30px' }}>
        <button style={styles.button}>Get Started</button>
        <Link to="/admin/dashboard" style={{ ...styles.button, backgroundColor: '#333', marginLeft: '10px' }}>Admin Panel</Link>
      </div>

      <div style={{ marginTop: '50px', display: 'flex', justifyContent: 'center', gap: '30px' }}>
        <div style={styles.card}>
          <h3>Anonymous</h3>
          <p>No logs, strict privacy.</p>
        </div>
        <div style={styles.card}>
          <h3>Fast</h3>
          <p>Gigabit servers worldwide.</p>
        </div>
        <div style={styles.card}>
          <h3>Secure</h3>
          <p>Military-grade encryption.</p>
        </div>
      </div>
    </div>
  );
};

const styles = {
  button: {
    padding: '10px 20px',
    fontSize: '16px',
    backgroundColor: '#007bff',
    color: '#fff',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    textDecoration: 'none'
  },
  card: {
    border: '1px solid #ddd',
    padding: '20px',
    borderRadius: '8px',
    width: '200px',
    boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
  }
};

export default Home;
