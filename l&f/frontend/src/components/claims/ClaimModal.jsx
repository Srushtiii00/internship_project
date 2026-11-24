import React, { useState } from 'react';
import { claimApi } from '../../api/claimApi';
import { X, ShieldCheck, AlertCircle } from 'lucide-react';

const ClaimModal = ({ item, isOpen, onClose, onSuccess }) => {
  const [proofDescription, setProofDescription] = useState('');
  const [contactInfo, setContactInfo] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen || !item) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!proofDescription.trim()) {
      setError('Please provide proof of ownership description.');
      return;
    }
    if (!contactInfo.trim()) {
      setError('Please provide your contact information (phone or email).');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const res = await claimApi.submitClaim({
        itemId: item.id,
        proofDescription: proofDescription.trim(),
        contactInfo: contactInfo.trim(),
      });

      if (res.success) {
        onSuccess(res.message || 'Claim submitted successfully!');
        onClose();
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to submit claim');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-content">
        <div className="modal-header">
          <div className="modal-title-group">
            <ShieldCheck size={22} className="text-primary" />
            <h3 className="modal-title">Submit Ownership Claim</h3>
          </div>
          <button onClick={onClose} className="btn-close-modal" aria-label="Close modal">
            <X size={20} />
          </button>
        </div>

        <div className="modal-item-summary">
          <p className="summary-title">{item.title}</p>
          <span className="summary-meta">Category: {item.category} | Location: {item.location}</span>
        </div>

        {error && (
          <div className="alert alert-error">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label className="form-label" htmlFor="proofDescription">
              Proof of Ownership <span className="required-star">*</span>
            </label>
            <textarea
              id="proofDescription"
              rows="4"
              className="form-textarea"
              placeholder="Describe unique identifiers, distinguishing marks, serial numbers, screen wallpapers, or specific contents inside to verify you are the rightful owner..."
              value={proofDescription}
              onChange={(e) => setProofDescription(e.target.value)}
              required
            ></textarea>
            <small className="form-hint">
              Be specific. This information is shared securely with the item finder to verify your claim.
            </small>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="contactInfo">
              Your Contact Information <span className="required-star">*</span>
            </label>
            <input
              id="contactInfo"
              type="text"
              className="form-input"
              placeholder="e.g. +1 555-0199 or name@example.com"
              value={contactInfo}
              onChange={(e) => setContactInfo(e.target.value)}
              required
            />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn btn-secondary" disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Submitting Claim...' : 'Submit Claim'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ClaimModal;
