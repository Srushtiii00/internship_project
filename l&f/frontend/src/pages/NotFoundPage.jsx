import React from 'react';
import { Link } from 'react-router-dom';
import { Search, Home, ArrowLeft } from 'lucide-react';

const NotFoundPage = () => {
  return (
    <div className="container py-16 text-center">
      <div className="not-found-box">
        <h1 className="not-found-code">404</h1>
        <h2 className="not-found-title">Page Not Found</h2>
        <p className="text-muted mt-2">
          The page you are looking for doesn't exist or has been moved.
        </p>
        <div className="not-found-actions mt-6">
          <Link to="/" className="btn btn-primary">
            <Home size={18} /> Return Home
          </Link>
          <Link to="/browse" className="btn btn-secondary">
            <Search size={18} /> Browse Listings
          </Link>
        </div>
      </div>
    </div>
  );
};

export default NotFoundPage;
