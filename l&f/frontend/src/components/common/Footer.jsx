import React from 'react';
import { Link } from 'react-router-dom';
import { Search, Heart } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="footer-container">
      <div className="container footer-content">
        <div className="footer-col brand-col">
          <div className="footer-brand">
            <div className="brand-icon small">
              <Search size={18} color="#ffffff" />
            </div>
            <span className="brand-title">Lost & Found Portal</span>
          </div>
          <p className="footer-desc">
            A trusted community platform dedicated to helping people reconnect with their lost belongings
            and report found items safely and transparently.
          </p>
        </div>

        <div className="footer-col">
          <h4 className="footer-heading">Quick Links</h4>
          <ul className="footer-links">
            <li><Link to="/">Home</Link></li>
            <li><Link to="/browse">Browse Listings</Link></li>
            <li><Link to="/report">Report an Item</Link></li>
            <li><Link to="/dashboard">User Dashboard</Link></li>
          </ul>
        </div>

        <div className="footer-col">
          <h4 className="footer-heading">Item Categories</h4>
          <ul className="footer-links">
            <li><Link to="/browse?category=Electronics">Electronics</Link></li>
            <li><Link to="/browse?category=Wallets%20%26%20Bags">Wallets & Bags</Link></li>
            <li><Link to="/browse?category=Documents">Documents & IDs</Link></li>
            <li><Link to="/browse?category=Keys">Keys</Link></li>
          </ul>
        </div>
      </div>

      <div className="footer-bottom">
        <div className="container bottom-content">
          <p>© {new Date().getFullYear()} Lost & Found Portal. Built with Spring Boot & React.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
