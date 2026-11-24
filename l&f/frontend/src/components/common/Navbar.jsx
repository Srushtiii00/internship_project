import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Search, PlusCircle, LayoutDashboard, Shield, LogOut, User, Menu, X } from 'lucide-react';

const Navbar = () => {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const closeMenu = () => setMobileMenuOpen(false);

  return (
    <header className="navbar-container">
      <div className="container navbar-content">
        <Link to="/" className="navbar-brand" onClick={closeMenu}>
          <div className="brand-icon">
            <Search size={22} color="#ffffff" />
          </div>
          <span className="brand-title">Lost & Found</span>
        </Link>

        {/* Desktop Navigation Links */}
        <nav className="desktop-nav">
          <Link to="/" className="nav-link">Home</Link>
          <Link to="/browse" className="nav-link">Browse Items</Link>
          <Link to="/report" className="nav-btn-report">
            <PlusCircle size={17} />
            <span>Report Item</span>
          </Link>
        </nav>

        {/* Auth Actions (Desktop) */}
        <div className="desktop-auth">
          {isAuthenticated ? (
            <div className="user-menu-wrapper">
              <Link to="/dashboard" className="nav-link nav-icon-link" title="User Dashboard">
                <LayoutDashboard size={18} />
                <span>Dashboard</span>
              </Link>

              {isAdmin && (
                <Link to="/admin" className="nav-link nav-icon-link admin-pill" title="Admin Dashboard">
                  <Shield size={16} />
                  <span>Admin</span>
                </Link>
              )}

              <div className="user-pill">
                <User size={16} className="text-muted" />
                <span className="username-text">{user?.fullName || user?.username}</span>
              </div>

              <button onClick={handleLogout} className="btn-logout" title="Sign Out">
                <LogOut size={17} />
              </button>
            </div>
          ) : (
            <div className="guest-actions">
              <Link to="/login" className="btn btn-ghost">Log In</Link>
              <Link to="/register" className="btn btn-primary">Sign Up</Link>
            </div>
          )}
        </div>

        {/* Mobile Hamburger Button */}
        <button
          className="mobile-toggle-btn"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle navigation menu"
        >
          {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="mobile-drawer">
          <Link to="/" className="mobile-link" onClick={closeMenu}>Home</Link>
          <Link to="/browse" className="mobile-link" onClick={closeMenu}>Browse Listings</Link>
          <Link to="/report" className="mobile-link highlight" onClick={closeMenu}>
            <PlusCircle size={18} /> Report Lost/Found
          </Link>
          <div className="mobile-divider"></div>
          {isAuthenticated ? (
            <>
              <Link to="/dashboard" className="mobile-link" onClick={closeMenu}>
                <LayoutDashboard size={18} /> My Dashboard
              </Link>
              {isAdmin && (
                <Link to="/admin" className="mobile-link admin-link" onClick={closeMenu}>
                  <Shield size={18} /> Admin Dashboard
                </Link>
              )}
              <div className="mobile-user-info">
                <span>Signed in as <strong>{user?.fullName || user?.username}</strong></span>
              </div>
              <button
                onClick={() => {
                  closeMenu();
                  handleLogout();
                }}
                className="btn btn-danger-outline btn-full"
              >
                <LogOut size={18} /> Sign Out
              </button>
            </>
          ) : (
            <div className="mobile-auth-btns">
              <Link to="/login" className="btn btn-outline btn-full" onClick={closeMenu}>Log In</Link>
              <Link to="/register" className="btn btn-primary btn-full" onClick={closeMenu}>Sign Up</Link>
            </div>
          )}
        </div>
      )}
    </header>
  );
};

export default Navbar;
