import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { UserPlus, AlertCircle, CheckCircle, Search } from 'lucide-react';

const RegisterPage = () => {
  const navigate = useNavigate();
  const { register, login } = useAuth();

  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }

    setLoading(true);

    try {
      const regData = {
        fullName: formData.fullName.trim(),
        username: formData.username.trim(),
        email: formData.email.trim(),
        phoneNumber: formData.phoneNumber.trim(),
        password: formData.password,
      };

      const res = await register(regData);
      if (res.success) {
        setSuccess('Registration successful! Logging you in...');
        // Auto-login
        setTimeout(async () => {
          try {
            await login(formData.username.trim(), formData.password);
            navigate('/dashboard');
          } catch {
            navigate('/login');
          }
        }, 1200);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Registration failed.');
      setLoading(false);
    }
  };

  return (
    <div className="auth-page-container">
      <div className="auth-card">
        <div className="auth-brand">
          <div className="brand-icon">
            <Search size={22} color="#ffffff" />
          </div>
          <span className="brand-title">Lost & Found Portal</span>
        </div>

        <h1 className="auth-title">Create an Account</h1>
        <p className="auth-subtitle">Join the portal to report items and connect with finders</p>

        {error && (
          <div className="alert alert-error mb-4">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="alert alert-success mb-4">
            <CheckCircle size={18} />
            <span>{success}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label className="form-label" htmlFor="fullName">Full Name <span className="required-star">*</span></label>
            <input
              id="fullName"
              type="text"
              name="fullName"
              className="form-input"
              placeholder="e.g. Jane Doe"
              value={formData.fullName}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label" htmlFor="username">Username <span className="required-star">*</span></label>
              <input
                id="username"
                type="text"
                name="username"
                className="form-input"
                placeholder="janedoe"
                value={formData.username}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="phoneNumber">Phone (Optional)</label>
              <input
                id="phoneNumber"
                type="text"
                name="phoneNumber"
                className="form-input"
                placeholder="+1 555-0199"
                value={formData.phoneNumber}
                onChange={handleChange}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="email">Email Address <span className="required-star">*</span></label>
            <input
              id="email"
              type="email"
              name="email"
              className="form-input"
              placeholder="jane@example.com"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label" htmlFor="password">Password <span className="required-star">*</span></label>
              <input
                id="password"
                type="password"
                name="password"
                className="form-input"
                placeholder="Min. 6 chars"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="confirmPassword">Confirm Password <span className="required-star">*</span></label>
              <input
                id="confirmPassword"
                type="password"
                name="confirmPassword"
                className="form-input"
                placeholder="Repeat password"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-full mt-4" disabled={loading}>
            {loading ? 'Creating Account...' : 'Sign Up'}
          </button>
        </form>

        <div className="auth-footer mt-6">
          <p>
            Already have an account?{' '}
            <Link to="/login" className="auth-link">
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
