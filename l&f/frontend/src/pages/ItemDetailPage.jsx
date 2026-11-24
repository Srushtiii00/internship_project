import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { itemApi } from '../api/itemApi';
import { claimApi } from '../api/claimApi';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/common/StatusBadge';
import ClaimModal from '../components/claims/ClaimModal';
import {
  MapPin,
  Calendar,
  Tag,
  User,
  ShieldCheck,
  Trash2,
  Edit,
  CheckCircle,
  XCircle,
  AlertTriangle,
  ArrowLeft,
  Image as ImageIcon,
} from 'lucide-react';
import { API_BASE_URL } from '../api/axiosClient';

const ItemDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated, isAdmin } = useAuth();

  const [item, setItem] = useState(null);
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState({ type: '', text: '' });
  const [isClaimModalOpen, setIsClaimModalOpen] = useState(false);
  const [processingClaimId, setProcessingClaimId] = useState(null);

  const isOwner = user && item && item.reporter && user.id === item.reporter.id;
  const canManage = isOwner || isAdmin;

  const fetchItemData = useCallback(async () => {
    setLoading(true);
    try {
      const res = await itemApi.getItemById(id);
      if (res.success && res.data) {
        setItem(res.data);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Item not found');
    } finally {
      setLoading(false);
    }
  }, [id]);

  const fetchItemClaims = useCallback(async () => {
    if (canManage) {
      try {
        const res = await claimApi.getClaimsForItem(id);
        if (res.success && res.data) {
          setClaims(res.data);
        }
      } catch (err) {
        console.error('Error fetching claims for item:', err);
      }
    }
  }, [id, canManage]);

  useEffect(() => {
    fetchItemData();
  }, [fetchItemData]);

  useEffect(() => {
    if (item && canManage) {
      fetchItemClaims();
    }
  }, [item, canManage, fetchItemClaims]);

  const handleDeleteItem = async () => {
    if (!window.confirm('Are you sure you want to permanently delete this listing?')) {
      return;
    }
    try {
      await itemApi.deleteItem(id);
      navigate('/browse', { replace: true });
    } catch (err) {
      setFeedback({ type: 'error', text: err.response?.data?.message || 'Failed to delete item' });
    }
  };

  const handleUpdateStatus = async (newStatus) => {
    try {
      const res = await itemApi.updateStatus(id, newStatus);
      if (res.success) {
        setItem(res.data);
        setFeedback({ type: 'success', text: `Status updated to ${newStatus}` });
      }
    } catch (err) {
      setFeedback({ type: 'error', text: err.response?.data?.message || 'Failed to update status' });
    }
  };

  const handleReviewClaim = async (claimId, newStatus) => {
    let remarks = '';
    if (newStatus === 'APPROVED') {
      remarks = prompt('Enter any contact or handover notes for the claimant:', 'Verified ownership. Please contact me for collection.') || '';
    } else {
      remarks = prompt('Enter reason for rejecting this claim (optional):', 'Details provided do not match the item.') || '';
    }

    setProcessingClaimId(claimId);
    try {
      const res = await claimApi.updateClaimStatus(claimId, newStatus, remarks);
      if (res.success) {
        setFeedback({
          type: 'success',
          text: `Claim ${newStatus.toLowerCase()} successfully!`,
        });
        fetchItemData();
        fetchItemClaims();
      }
    } catch (err) {
      setFeedback({ type: 'error', text: err.response?.data?.message || 'Failed to update claim' });
    } finally {
      setProcessingClaimId(null);
    }
  };

  if (loading) {
    return (
      <div className="flex-center py-16">
        <div className="spinner"></div>
      </div>
    );
  }

  if (error || !item) {
    return (
      <div className="container py-12 text-center">
        <h2>Item Not Found</h2>
        <p className="text-muted mt-2">{error || "The requested item listing does not exist."}</p>
        <Link to="/browse" className="btn btn-primary mt-4">
          <ArrowLeft size={16} /> Back to Browse
        </Link>
      </div>
    );
  }

  const imageUrl = item.imageUrl
    ? (item.imageUrl.startsWith('http') ? item.imageUrl : `${API_BASE_URL}${item.imageUrl}`)
    : null;

  return (
    <div className="container item-detail-page py-8">
      <Link to="/browse" className="back-link mb-4">
        <ArrowLeft size={16} /> Back to Listings
      </Link>

      {feedback.text && (
        <div className={`alert alert-${feedback.type} mb-6`}>
          <span>{feedback.text}</span>
        </div>
      )}

      <div className="detail-layout">
        {/* Left Column: Image Card */}
        <div className="detail-media-card">
          <div className="detail-image-wrapper">
            {imageUrl ? (
              <img src={imageUrl} alt={item.title} className="detail-main-img" />
            ) : (
              <div className="detail-image-placeholder">
                <ImageIcon size={64} className="text-muted" />
                <span>No photo provided for this listing</span>
              </div>
            )}
            <div className="detail-badge-strip">
              <span className={`pill-type ${item.type === 'LOST' ? 'type-lost' : 'type-found'}`}>
                {item.type} ITEM
              </span>
              <StatusBadge status={item.status} />
            </div>
          </div>
        </div>

        {/* Right Column: Information & Actions */}
        <div className="detail-info-card">
          <div className="detail-header">
            <span className="category-pill-lg">
              <Tag size={15} /> {item.category}
            </span>
            <h1 className="detail-title">{item.title}</h1>
          </div>

          <div className="detail-meta-grid">
            <div className="meta-box">
              <MapPin size={18} className="meta-icon" />
              <div>
                <span className="meta-label">Location</span>
                <span className="meta-val">{item.location}</span>
              </div>
            </div>

            <div className="meta-box">
              <Calendar size={18} className="meta-icon" />
              <div>
                <span className="meta-label">{item.type === 'LOST' ? 'Date Lost' : 'Date Found'}</span>
                <span className="meta-val">{item.itemDate}</span>
              </div>
            </div>

            <div className="meta-box">
              <User size={18} className="meta-icon" />
              <div>
                <span className="meta-label">Reported By</span>
                <span className="meta-val">{item.reporter?.fullName || item.reporter?.username}</span>
              </div>
            </div>
          </div>

          <div className="detail-desc-section">
            <h3 className="section-heading">Detailed Description</h3>
            <p className="detail-description-text">{item.description}</p>
          </div>

          {/* Action Area */}
          <div className="detail-actions-area">
            {/* If user can manage (owner or admin) */}
            {canManage ? (
              <div className="owner-action-panel">
                <h4 className="panel-heading">Listing Management</h4>
                <div className="owner-buttons">
                  <Link to={`/edit-item/${item.id}`} className="btn btn-secondary">
                    <Edit size={16} /> Edit Details
                  </Link>

                  {item.status !== 'RECOVERED' ? (
                    <button
                      onClick={() => handleUpdateStatus('RECOVERED')}
                      className="btn btn-success"
                    >
                      <CheckCircle size={16} /> Mark as Recovered
                    </button>
                  ) : (
                    <button
                      onClick={() => handleUpdateStatus(item.type === 'LOST' ? 'LOST' : 'FOUND')}
                      className="btn btn-secondary"
                    >
                      Reopen Listing
                    </button>
                  )}

                  <button onClick={handleDeleteItem} className="btn btn-danger-outline">
                    <Trash2 size={16} /> Delete
                  </button>
                </div>
              </div>
            ) : (
              /* Public / Claimant actions */
              <div className="claim-action-panel">
                {item.status === 'RECOVERED' ? (
                  <div className="banner banner-recovered">
                    <CheckCircle size={20} />
                    <span>This item has been successfully recovered and verified!</span>
                  </div>
                ) : (
                  <>
                    {isAuthenticated ? (
                      <button
                        onClick={() => setIsClaimModalOpen(true)}
                        className="btn btn-primary btn-lg btn-full"
                      >
                        <ShieldCheck size={20} />
                        <span>This is Mine! Claim Item</span>
                      </button>
                    ) : (
                      <div className="login-to-claim-box">
                        <p>Do you own this item? Sign in to submit a verified ownership claim.</p>
                        <Link to="/login" className="btn btn-primary btn-full mt-2">
                          Log In to Submit Claim
                        </Link>
                      </div>
                    )}
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Claims Management for Item Owner */}
      {canManage && (
        <section className="item-claims-section mt-10">
          <div className="section-header">
            <div>
              <h2 className="section-title">Ownership Claims ({claims.length})</h2>
              <p className="section-subtitle">
                Review claims submitted by community members for this item
              </p>
            </div>
          </div>

          {claims.length > 0 ? (
            <div className="claims-list-grid">
              {claims.map((claim) => (
                <div key={claim.id} className="claim-review-card">
                  <div className="claim-card-header">
                    <div className="claimant-info">
                      <strong>{claim.claimant?.fullName || claim.claimant?.username}</strong>
                      <span className="claim-date text-muted">Submitted on {new Date(claim.createdAt).toLocaleDateString()}</span>
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
                    <strong>Contact Details:</strong> {claim.contactInfo}
                  </div>

                  {claim.remarks && (
                    <div className="claim-remarks-box">
                      <strong>Decision Remarks:</strong> {claim.remarks}
                    </div>
                  )}

                  {claim.status === 'PENDING' && (
                    <div className="claim-actions-row">
                      <button
                        onClick={() => handleReviewClaim(claim.id, 'APPROVED')}
                        disabled={processingClaimId === claim.id}
                        className="btn btn-success btn-sm"
                      >
                        <CheckCircle size={15} /> Approve Claim
                      </button>
                      <button
                        onClick={() => handleReviewClaim(claim.id, 'REJECTED')}
                        disabled={processingClaimId === claim.id}
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
              <ShieldCheck size={40} className="text-muted" />
              <p>No claims have been submitted for this item yet.</p>
            </div>
          )}
        </section>
      )}

      {/* Claim Modal Popup */}
      <ClaimModal
        item={item}
        isOpen={isClaimModalOpen}
        onClose={() => setIsClaimModalOpen(false)}
        onSuccess={(msg) => {
          setFeedback({ type: 'success', text: msg });
          fetchItemData();
        }}
      />
    </div>
  );
};

export default ItemDetailPage;
