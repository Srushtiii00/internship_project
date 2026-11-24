import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { itemApi } from '../api/itemApi';
import { claimApi } from '../api/claimApi';
import { authApi } from '../api/authApi';
import StatusBadge from '../components/common/StatusBadge';
import {
  User,
  Package,
  ShieldCheck,
  Inbox,
  Edit,
  Trash2,
  CheckCircle,
  XCircle,
  PlusCircle,
  ExternalLink,
  Phone,
  Mail,
  AlertCircle,
} from 'lucide-react';

const UserDashboardPage = () => {
  const { user, updateUser } = useAuth();

  const [activeTab, setActiveTab] = useState('items'); // 'items' | 'my-claims' | 'received-claims' | 'profile'
  const [myItems, setMyItems] = useState([]);
  const [myClaims, setMyClaims] = useState([]);
  const [receivedClaims, setReceivedClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState({ type: '', text: '' });

  // Profile Edit State
  const [profileForm, setProfileForm] = useState({
    fullName: user?.fullName || '',
    phoneNumber: user?.phoneNumber || '',
  });
  const [savingProfile, setSavingProfile] = useState(false);

  const loadDashboardData = useCallback(async () => {
    setLoading(true);
    try {
      const [itemsRes, myClaimsRes, receivedRes] = await Promise.allSettled([
        itemApi.getMyItems(),
        claimApi.getMyClaims(),
        claimApi.getClaimsReceived(),
      ]);

      if (itemsRes.status === 'fulfilled' && itemsRes.value?.data) {
        setMyItems(itemsRes.value.data);
      }
      if (myClaimsRes.status === 'fulfilled' && myClaimsRes.value?.data) {
        setMyClaims(myClaimsRes.value.data);
      }
      if (receivedRes.status === 'fulfilled' && receivedRes.value?.data) {
        setReceivedClaims(receivedRes.value.data);
      }
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboardData();
  }, [loadDashboardData]);

  const handleDeleteItem = async (itemId) => {
    if (!window.confirm('Are you sure you want to delete this listing?')) return;
    try {
      const res = await itemApi.deleteItem(itemId);
      if (res.success) {
        setFeedback({ type: 'success', text: 'Item listing removed' });
        loadDashboardData();
      }
    } catch (err) {
      setFeedback({ type: 'error', text: err.response?.data?.message || 'Failed to delete' });
    }
  };

  const handleUpdateItemStatus = async (itemId, newStatus) => {
    try {
      const res = await itemApi.updateStatus(itemId, newStatus);
      if (res.success) {
        setFeedback({ type: 'success', text: `Item marked as ${newStatus}` });
        loadDashboardData();
      }
    } catch (err) {
      setFeedback({ type: 'error', text: err.response?.data?.message || 'Failed to update' });
    }
  };

  const handleReviewClaim = async (claimId, status) => {
    let remarks = '';
    if (status === 'APPROVED') {
      remarks = prompt('Enter notes for claimant (e.g. handover place):', 'Approved. Please call me.') || '';
    } else {
      remarks = prompt('Enter reason for rejection (optional):', 'Proof did not match.') || '';
    }

    try {
      const res = await claimApi.updateClaimStatus(claimId, status, remarks);
      if (res.success) {
        setFeedback({ type: 'success', text: `Claim ${status.toLowerCase()} successfully!` });
        loadDashboardData();
      }
    } catch (err) {
      setFeedback({ type: 'error', text: err.response?.data?.message || 'Failed to update claim' });
    }
  };

  const handleProfileSave = async (e) => {
    e.preventDefault();
    setSavingProfile(true);
    try {
      const res = await authApi.updateProfile(profileForm);
      if (res.success && res.data) {
        updateUser(res.data);
        setFeedback({ type: 'success', text: 'Profile updated successfully!' });
      }
    } catch (err) {
      setFeedback({ type: 'error', text: err.response?.data?.message || 'Failed to update profile' });
    } finally {
      setSavingProfile(false);
    }
  };

  return (
    <div className="container dashboard-page py-8">
      {/* Header Profile Greeting */}
      <div className="dashboard-header mb-6">
        <div>
          <h1 className="page-title">User Dashboard</h1>
          <p className="page-subtitle">
            Welcome back, <strong>{user?.fullName || user?.username}</strong>! Manage your reported items and claims.
          </p>
        </div>
        <Link to="/report" className="btn btn-primary">
          <PlusCircle size={18} /> Report New Item
        </Link>
      </div>

      {feedback.text && (
        <div className={`alert alert-${feedback.type} mb-6`}>
          <span>{feedback.text}</span>
        </div>
      )}

      {/* Dashboard Tabs Bar */}
      <div className="dashboard-tabs">
        <button
          className={`dash-tab ${activeTab === 'items' ? 'active' : ''}`}
          onClick={() => setActiveTab('items')}
        >
          <Package size={18} />
          <span>My Listings ({myItems.length})</span>
        </button>

        <button
          className={`dash-tab ${activeTab === 'my-claims' ? 'active' : ''}`}
          onClick={() => setActiveTab('my-claims')}
        >
          <ShieldCheck size={18} />
          <span>Claims Submitted ({myClaims.length})</span>
        </button>

        <button
          className={`dash-tab ${activeTab === 'received-claims' ? 'active' : ''}`}
          onClick={() => setActiveTab('received-claims')}
        >
          <Inbox size={18} />
          <span>Claims Received ({receivedClaims.length})</span>
        </button>

        <button
          className={`dash-tab ${activeTab === 'profile' ? 'active' : ''}`}
          onClick={() => setActiveTab('profile')}
        >
          <User size={18} />
          <span>My Profile</span>
        </button>
      </div>

      {/* Tab Content */}
      <div className="dashboard-tab-content mt-6">
        {loading ? (
          <div className="flex-center py-12">
            <div className="spinner"></div>
          </div>
        ) : (
          <>
            {/* TAB 1: My Reported Items */}
            {activeTab === 'items' && (
              <div>
                {myItems.length > 0 ? (
                  <div className="dashboard-table-card">
                    <table className="dash-table">
                      <thead>
                        <tr>
                          <th>Item</th>
                          <th>Category</th>
                          <th>Type</th>
                          <th>Date</th>
                          <th>Status</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {myItems.map((item) => (
                          <tr key={item.id}>
                            <td className="item-title-col">
                              <Link to={`/items/${item.id}`} className="dash-table-link">
                                {item.title}
                              </Link>
                              <span className="location-hint text-muted">{item.location}</span>
                            </td>
                            <td>{item.category}</td>
                            <td>
                              <span className={`pill-type ${item.type === 'LOST' ? 'type-lost' : 'type-found'}`}>
                                {item.type}
                              </span>
                            </td>
                            <td>{item.itemDate}</td>
                            <td>
                              <StatusBadge status={item.status} />
                            </td>
                            <td className="actions-cell">
                              <Link to={`/edit-item/${item.id}`} className="btn-icon" title="Edit Item">
                                <Edit size={16} />
                              </Link>

                              {item.status !== 'RECOVERED' && (
                                <button
                                  onClick={() => handleUpdateItemStatus(item.id, 'RECOVERED')}
                                  className="btn-icon success-icon"
                                  title="Mark as Recovered"
                                >
                                  <CheckCircle size={16} />
                                </button>
                              )}

                              <button
                                onClick={() => handleDeleteItem(item.id)}
                                className="btn-icon danger-icon"
                                title="Delete Item"
                              >
                                <Trash2 size={16} />
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <div className="empty-state-box">
                    <Package size={48} className="text-muted" />
                    <h3>No items reported yet</h3>
                    <p>Have you lost or found something? Submit a report to get started.</p>
                    <Link to="/report" className="btn btn-primary mt-4">
                      Report an Item
                    </Link>
                  </div>
                )}
              </div>
            )}

            {/* TAB 2: Claims Submitted by Me */}
            {activeTab === 'my-claims' && (
              <div>
                {myClaims.length > 0 ? (
                  <div className="claims-grid">
                    {myClaims.map((claim) => (
                      <div key={claim.id} className="claim-item-card">
                        <div className="claim-item-header">
                          <div>
                            <Link to={`/items/${claim.itemId}`} className="claim-item-title">
                              {claim.itemTitle} <ExternalLink size={14} />
                            </Link>
                            <span className="claim-meta-text">
                              Category: {claim.itemCategory} | Location: {claim.itemLocation}
                            </span>
                          </div>
                          <span className={`badge badge-${claim.status.toLowerCase()}`}>
                            {claim.status}
                          </span>
                        </div>

                        <div className="claim-body-section">
                          <span className="label-bold">Your Proof Description:</span>
                          <p className="claim-proof-text">{claim.proofDescription}</p>
                        </div>

                        {claim.status === 'APPROVED' && (
                          <div className="claim-success-box">
                            <CheckCircle size={18} className="text-green" />
                            <div>
                              <strong>Claim Approved!</strong>
                              {claim.remarks && <p className="notes-text">Finder notes: {claim.remarks}</p>}
                            </div>
                          </div>
                        )}

                        {claim.status === 'REJECTED' && (
                          <div className="claim-rejected-box">
                            <XCircle size={18} className="text-red" />
                            <div>
                              <strong>Claim Declined</strong>
                              {claim.remarks && <p className="notes-text">Reason: {claim.remarks}</p>}
                            </div>
                          </div>
                        )}

                        <div className="claim-card-footer">
                          <small className="text-muted">
                            Submitted on {new Date(claim.createdAt).toLocaleDateString()}
                          </small>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="empty-state-box">
                    <ShieldCheck size={48} className="text-muted" />
                    <h3>No claims submitted</h3>
                    <p>If you see a found item that belongs to you, click "Claim Item" on its detail page.</p>
                    <Link to="/browse?type=FOUND" className="btn btn-primary mt-4">
                      Browse Found Items
                    </Link>
                  </div>
                )}
              </div>
            )}

            {/* TAB 3: Claims Received on My Items */}
            {activeTab === 'received-claims' && (
              <div>
                {receivedClaims.length > 0 ? (
                  <div className="claims-grid">
                    {receivedClaims.map((claim) => (
                      <div key={claim.id} className="claim-review-card">
                        <div className="claim-card-header">
                          <div>
                            <Link to={`/items/${claim.itemId}`} className="claim-item-title">
                              Item: {claim.itemTitle}
                            </Link>
                            <p className="text-muted text-sm mt-1">
                              Claimed by: <strong>{claim.claimant?.fullName || claim.claimant?.username}</strong>
                            </p>
                          </div>
                          <span className={`badge badge-${claim.status.toLowerCase()}`}>
                            {claim.status}
                          </span>
                        </div>

                        <div className="claim-proof-box">
                          <span className="proof-label">Proof of Ownership:</span>
                          <p className="proof-text">{claim.proofDescription}</p>
                        </div>

                        <div className="claim-contact-box">
                          <strong>Contact Info:</strong> {claim.contactInfo}
                        </div>

                        {claim.remarks && (
                          <div className="claim-remarks-box">
                            <strong>Your Decision Notes:</strong> {claim.remarks}
                          </div>
                        )}

                        {claim.status === 'PENDING' && (
                          <div className="claim-actions-row mt-4">
                            <button
                              onClick={() => handleReviewClaim(claim.id, 'APPROVED')}
                              className="btn btn-success btn-sm"
                            >
                              <CheckCircle size={15} /> Approve Claim
                            </button>
                            <button
                              onClick={() => handleReviewClaim(claim.id, 'REJECTED')}
                              className="btn btn-danger-outline btn-sm"
                            >
                              <XCircle size={15} /> Reject
                            </button>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="empty-state-box">
                    <Inbox size={48} className="text-muted" />
                    <h3>No claims received yet</h3>
                    <p>When someone claims an item you reported, you will be able to review their proof here.</p>
                  </div>
                )}
              </div>
            )}

            {/* TAB 4: My Profile */}
            {activeTab === 'profile' && (
              <div className="profile-tab-card">
                <h3 className="section-heading mb-4">Account Information</h3>
                <form onSubmit={handleProfileSave} className="standard-form profile-form">
                  <div className="form-group">
                    <label className="form-label">Username</label>
                    <input
                      type="text"
                      className="form-input bg-disabled"
                      value={user?.username || ''}
                      disabled
                    />
                    <small className="form-hint">Username cannot be changed</small>
                  </div>

                  <div className="form-group">
                    <label className="form-label">Email Address</label>
                    <input
                      type="email"
                      className="form-input bg-disabled"
                      value={user?.email || ''}
                      disabled
                    />
                    <small className="form-hint">Email address cannot be changed</small>
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="fullName">Full Name</label>
                    <input
                      id="fullName"
                      type="text"
                      className="form-input"
                      value={profileForm.fullName}
                      onChange={(e) => setProfileForm({ ...profileForm, fullName: e.target.value })}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="phoneNumber">Contact Phone Number</label>
                    <input
                      id="phoneNumber"
                      type="text"
                      className="form-input"
                      placeholder="e.g. +1 555-0199"
                      value={profileForm.phoneNumber}
                      onChange={(e) => setProfileForm({ ...profileForm, phoneNumber: e.target.value })}
                    />
                  </div>

                  <div className="form-actions">
                    <button type="submit" className="btn btn-primary" disabled={savingProfile}>
                      {savingProfile ? 'Saving...' : 'Save Profile Changes'}
                    </button>
                  </div>
                </form>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default UserDashboardPage;
