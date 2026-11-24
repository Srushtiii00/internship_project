import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { itemApi } from '../api/itemApi';
import { Upload, AlertCircle, CheckCircle2, Image as ImageIcon, X } from 'lucide-react';

const CATEGORIES = [
  'Electronics',
  'Wallets & Bags',
  'Documents',
  'Keys',
  'Clothing',
  'Jewelry & Accessories',
  'Books & Stationery',
  'Others',
];

const ReportItemPage = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: 'Electronics',
    type: 'LOST',
    location: '',
    itemDate: new Date().toISOString().split('T')[0],
  });

  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        setError('Image file size must be less than 5MB.');
        return;
      }
      setImageFile(file);
      setImagePreview(URL.createObjectURL(file));
      setError('');
    }
  };

  const removeImage = () => {
    setImageFile(null);
    if (imagePreview) {
      URL.revokeObjectURL(imagePreview);
      setImagePreview(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const data = new FormData();
      data.append('title', formData.title.trim());
      data.append('description', formData.description.trim());
      data.append('category', formData.category);
      data.append('type', formData.type);
      data.append('location', formData.location.trim());
      data.append('itemDate', formData.itemDate);

      if (imageFile) {
        data.append('image', imageFile);
      }

      const res = await itemApi.createItem(data);
      if (res.success && res.data) {
        navigate(`/items/${res.data.id}`);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to submit report');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container form-page-container py-8">
      <div className="form-card">
        <div className="form-header">
          <h1 className="form-title">Report an Item</h1>
          <p className="form-subtitle">
            Provide details about the item you lost or found to help reconnect with its owner
          </p>
        </div>

        {error && (
          <div className="alert alert-error mb-6">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="standard-form">
          {/* Item Type Toggle */}
          <div className="form-group">
            <label className="form-label">I want to report a: <span className="required-star">*</span></label>
            <div className="type-radio-grid">
              <label className={`type-radio-card ${formData.type === 'LOST' ? 'selected-lost' : ''}`}>
                <input
                  type="radio"
                  name="type"
                  value="LOST"
                  checked={formData.type === 'LOST'}
                  onChange={handleChange}
                  className="sr-only"
                />
                <span className="radio-title">Lost Item</span>
                <span className="radio-sub">I misplaced or lost my personal belonging</span>
              </label>

              <label className={`type-radio-card ${formData.type === 'FOUND' ? 'selected-found' : ''}`}>
                <input
                  type="radio"
                  name="type"
                  value="FOUND"
                  checked={formData.type === 'FOUND'}
                  onChange={handleChange}
                  className="sr-only"
                />
                <span className="radio-title">Found Item</span>
                <span className="radio-sub">I discovered an unattended item and want to return it</span>
              </label>
            </div>
          </div>

          {/* Title */}
          <div className="form-group">
            <label className="form-label" htmlFor="title">
              Item Title <span className="required-star">*</span>
            </label>
            <input
              id="title"
              type="text"
              name="title"
              className="form-input"
              placeholder="e.g. Blue Hydro Flask Water Bottle or Brown Leather Wallet"
              value={formData.title}
              onChange={handleChange}
              required
              maxLength={150}
            />
          </div>

          {/* Category & Date Grid */}
          <div className="form-row-2">
            <div className="form-group">
              <label className="form-label" htmlFor="category">
                Category <span className="required-star">*</span>
              </label>
              <select
                id="category"
                name="category"
                className="form-select"
                value={formData.category}
                onChange={handleChange}
                required
              >
                {CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="itemDate">
                Date {formData.type === 'LOST' ? 'Lost' : 'Found'} <span className="required-star">*</span>
              </label>
              <input
                id="itemDate"
                type="date"
                name="itemDate"
                className="form-input"
                value={formData.itemDate}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          {/* Location */}
          <div className="form-group">
            <label className="form-label" htmlFor="location">
              Location <span className="required-star">*</span>
            </label>
            <input
              id="location"
              type="text"
              name="location"
              className="form-input"
              placeholder="e.g. Science Complex 3rd Floor Lab, Main Library, or Bus Stop #4"
              value={formData.location}
              onChange={handleChange}
              required
              maxLength={150}
            />
          </div>

          {/* Description */}
          <div className="form-group">
            <label className="form-label" htmlFor="description">
              Detailed Description <span className="required-star">*</span>
            </label>
            <textarea
              id="description"
              name="description"
              rows="5"
              className="form-textarea"
              placeholder="Include color, brand, condition, identifying stickers, contents (without revealing secret info that only the true owner would know)..."
              value={formData.description}
              onChange={handleChange}
              required
            ></textarea>
          </div>

          {/* Image Upload */}
          <div className="form-group">
            <label className="form-label">Photo Upload (Optional)</label>
            <div className="file-dropzone">
              {imagePreview ? (
                <div className="preview-container">
                  <img src={imagePreview} alt="Item Preview" className="uploaded-preview-img" />
                  <button type="button" onClick={removeImage} className="btn-remove-preview">
                    <X size={16} /> Remove Photo
                  </button>
                </div>
              ) : (
                <label className="dropzone-label">
                  <Upload size={32} className="dropzone-icon" />
                  <span className="dropzone-text">Click to choose a photo or drag & drop</span>
                  <span className="dropzone-hint">PNG, JPG, WEBP up to 5MB</span>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleImageChange}
                    className="sr-only"
                  />
                </label>
              )}
            </div>
          </div>

          {/* Submit Button */}
          <div className="form-actions mt-6">
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="btn btn-secondary"
              disabled={loading}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Submitting Report...' : `Publish ${formData.type === 'LOST' ? 'Lost' : 'Found'} Report`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ReportItemPage;
