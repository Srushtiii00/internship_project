import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { adminApi } from '../api/adminApi';
import { itemApi } from '../api/itemApi';
import StatusBadge from '../components/common/StatusBadge';
import {
  Shield,
  Users,
  Package,
  CheckCircle,
  AlertTriangle,
  Trash2,
  ExternalLink,
  Search,
  RefreshCw,
} from 'lucide-react';

const AdminDashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [items, setItems] = useState([]);
  const [activeTab, setActiveTab] = useState('items'); // 'items' | 'users'
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState({ type: '', text: '' });
  const [filterKeyword, setFilterKeyword] = useState('');

  const loadAdminData = useCallback(async () => {
    setLoading(true);
    try {
      const [statsRes, usersRes, itemsRes] = await Promise.allSettled([
        adminApi.getStats(),
        adminApi.getUsers(),
        itemApi.getItems({ size: 100 }),
      ]);

      if (statsRes.status === 'fulfilled' && statsRes.value?.data) {
        setStats(statsRes.value.data);
      }
      if (usersRes.status === 'fulfilled' && usersRes.value?.data) {
        setUsers(usersRes.value.data);
      }
      if (itemsRes.status === 'fulfilled' && itemsRes.value?.data?.content) {
        setItems(itemsRes.value.data.content);
      }
    } catch (err) {
      console.error('Failed to load admin data:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAdminData();
  }, [loadAdminData]);

  const handleDeleteItem = async (itemId) => {
    if (!window.confirm('As an administrator, are you sure you want to remove this listing?')) return;
    try {
      const res = await adminApi.deleteItem(itemId);
      if (res.success) {
        setFeedback({ type: 'success', text: 'Listing removed by admin' });
        loadAdminData();
      }
    } catch (err) {
      setFeedback({ type: 'error', text: err.response?.data?.message || 'Failed to remove item' });
    }
  };

  const handleDeleteUser = async (userId, username) => {
    if (!window.confirm(`Are you sure you want to delete user account "${username}"?`)) return;
    try {
      const res = await adminApi.deleteUser(userId);
      if (res.success) {
        setFeedback({ type: 'success', text: `User ${username} deleted` });
        loadAdminData();
      }
    } catch (err) {
      setFeedback({ type: 'error', text: err.response?.data?.message || 'Failed to delete user' });
    }
  };

  const filteredItems = items.filter((it) => {
    if (!filterKeyword) return true;
    const kw = filterKeyword.toLowerCase();
    return (
      it.title.toLowerCase().includes(kw) ||
      it.category.toLowerCase().includes(kw) ||
      it.location.toLowerCase().includes(kw) ||
      it.reporter?.username?.toLowerCase().includes(kw)
    );
  });

  return (
    <div className="container admin-page py-8">
      <div className="page-header mb-6">
        <div className="admin-title-badge-group">
          <Shield size={28} className="text-primary" />
          <h1 className="page-title">Administrator Console</h1>
        </div>
        <p className="page-subtitle">
          Platform overview, content moderation, and user management
        </p>
      </div>

      {feedback.text && (
        <div className={`alert alert-${feedback.type} mb-6`}>
          <span>{feedback.text}</span>
        </div>
      )}

      {/* Metric Cards Grid */}
      {stats && (
        <div className="admin-stats-grid mb-8">
          <div className="stat-card">
            <span className="stat-value">{stats.totalItems}</span>
            <span className="stat-label">Total Listings</span>
          </div>
          <div className="stat-card">
            <span className="stat-value text-red">{stats.totalLost}</span>
            <span className="stat-label">Lost Reports</span>
          </div>
          <div className="stat-card">
            <span className="stat-value text-blue">{stats.totalFound}</span>
            <span className="stat-label">Found Reports</span>
          </div>
          <div className="stat-card">
            <span className="stat-value text-green">{stats.totalRecovered}</span>
            <span className="stat-label">Recovered</span>
          </div>
          <div className="stat-card">
            <span className="stat-value text-amber">{stats.pendingClaims}</span>
            <span className="stat-label">Pending Claims</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{stats.totalUsers}</span>
            <span className="stat-label">Registered Users</span>
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="dashboard-tabs">
        <button
          className={`dash-tab ${activeTab === 'items' ? 'active' : ''}`}
          onClick={() => setActiveTab('items')}
        >
          <Package size={18} />
          <span>Listing Moderation ({items.length})</span>
        </button>

        <button
          className={`dash-tab ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          <Users size={18} />
          <span>User Accounts ({users.length})</span>
        </button>
      </div>

      <div className="dashboard-tab-content mt-6">
        {loading ? (
          <div className="flex-center py-12">
            <div className="spinner"></div>
          </div>
        ) : (
          <>
            {activeTab === 'items' && (
              <div className="dashboard-table-card">
                <div className="table-search-bar">
                  <Search size={16} className="search-icon" />
                  <input
                    type="text"
                    placeholder="Search listings by title, category, location, or reporter..."
                    value={filterKeyword}
                    onChange={(e) => setFilterKeyword(e.target.value)}
                    className="filter-search-input"
                  />
                </div>

                <table className="dash-table">
                  <thead>
                    <tr>
                      <th>Title</th>
                      <th>Category</th>
                      <th>Type</th>
                      <th>Status</th>
                      <th>Reporter</th>
                      <th>Date</th>
                      <th>Moderate</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredItems.map((item) => (
                      <tr key={item.id}>
                        <td>
                          <Link to={`/items/${item.id}`} className="dash-table-link">
                            {item.title} <ExternalLink size={13} />
                          </Link>
                        </td>
                        <td>{item.category}</td>
                        <td>
                          <span className={`pill-type ${item.type === 'LOST' ? 'type-lost' : 'type-found'}`}>
                            {item.type}
                          </span>
                        </td>
                        <td>
                          <StatusBadge status={item.status} />
                        </td>
                        <td>{item.reporter?.fullName || item.reporter?.username}</td>
                        <td>{item.itemDate}</td>
                        <td>
                          <button
                            onClick={() => handleDeleteItem(item.id)}
                            className="btn-icon danger-icon"
                            title="Force Remove Listing"
                          >
                            <Trash2 size={16} />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {activeTab === 'users' && (
              <div className="dashboard-table-card">
                <table className="dash-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Username</th>
                      <th>Full Name</th>
                      <th>Email</th>
                      <th>Phone</th>
                      <th>Role</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((u) => (
                      <tr key={u.id}>
                        <td>#{u.id}</td>
                        <td><strong>{u.username}</strong></td>
                        <td>{u.fullName}</td>
                        <td>{u.email}</td>
                        <td>{u.phoneNumber || '—'}</td>
                        <td>
                          <span className={`badge ${u.role === 'ROLE_ADMIN' ? 'badge-admin' : 'badge-user'}`}>
                            {u.role}
                          </span>
                        </td>
                        <td>
                          {u.username !== 'admin' && (
                            <button
                              onClick={() => handleDeleteUser(u.id, u.username)}
                              className="btn-icon danger-icon"
                              title="Delete User Account"
                            >
                              <Trash2 size={16} />
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default AdminDashboardPage;
