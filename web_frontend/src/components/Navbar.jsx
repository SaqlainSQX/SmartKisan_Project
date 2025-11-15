import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Sprout, LogOut, MessageSquare, Home, CloudSun, User, LayoutGrid } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path ? 'active' : '';

  return (
    <nav className="navbar">
      <Link to="/" className="nav-brand">
        <Sprout size={28} />
        <span>SmartKisan</span>
      </Link>
      <div className="nav-links">
        <Link to="/" className={`nav-link ${isActive('/')}`}><Home size={20} /></Link>
        <Link to="/disease" className={`nav-link ${isActive('/disease')}`}><CloudSun size={20} /></Link>
        <Link to="/chatbot" className={`nav-link ${isActive('/chatbot')}`}><MessageSquare size={20} /></Link>
        <Link to="/forum" className={`nav-link ${isActive('/forum')}`}><LayoutGrid size={20} /></Link>
        <Link to="/profile" className={`nav-link ${isActive('/profile')}`}><User size={20} /></Link>
        <button onClick={handleLogout} className="btn-primary" style={{ padding: '8px 16px', background: 'transparent', border: '1px solid var(--primary-light)', color: 'var(--primary-light)', boxShadow: 'none' }}>
          <LogOut size={18} />
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
