import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { itemApi } from '../api/itemApi';
import { Upload, AlertCircle, X, ArrowLeft } from 'lucide-react';
import { API_BASE_URL } from '../api/axiosClient';

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

const EditItemPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: 'Electronics',
    type: 'LOST',
    location: '',
    itemDate: '',
  });

  const [existingImageUrl, setExistingImageUrl] = useState(null);
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchItem = async () => {
      try {
        const res = await itemApi.getItemById(id);
        if (res.success && res.data) {
          const it = res.data;
          setFormData({
            title: it.title,
            description: it.description,
            category: it.category,
            type: it.type,
            location: it.location,
            itemDate: it.itemDate,
          });
          if (it.imageUrl) {
            setExistingImageUrl(it.imageUrl.startsWith('http') ? it.imageUrl : `${API_BASE_URL}${it.imageUrl}`);
          }
        }
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load item for editing');
      } finally {
        setLoading(false);
      }
    };

    fetchItem();
  }, [id]);

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
    setSaving(true);
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

      const res = await itemApi.updateItem(id, data);
      if (res.success) {
        navigate(`/items/${id}`);
      }
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to update item');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex-center py-16">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div className="container form-page-container py-8">
      <div className="form-card">
        <button onClick={() => navigate(-1)} className="back-link mb-4">
          <ArrowLeft size={16} /> Cancel & Go Back
        </button>

        <div className="form-header">
          <h1 className="form-title">Edit Listing Details</h1>
          <p className="form-subtitle">Update information or replace photo for this report</p>
        </div>

        {error && (
          <div className="alert alert-error mb-6">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="standard-form">
          <div className="form-group">
            <label className="form-label" htmlFor="title">
              Item Title <span className="required-star">*</span>
            </label>
            <input
              id="title"
              type="text"
              name="title"
              className="form-input"
              value={formData.title}
              onChange={handleChange}
              required
              maxLength={150}
            />
          </div>

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
                Date <span className="required-star">*</span>
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

          <div className="form-group">
            <label className="form-label" htmlFor="location">
              Location <span className="required-star">*</span>
            </label>
            <input
              id="location"
              type="text"
              name="location"
              className="form-input"
              value={formData.location}
              onChange={handleChange}
              required
              maxLength={150}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="description">
              Detailed Description <span className="required-star">*</span>
            </label>
            <textarea
              id="description"
              name="description"
              rows="5"
              className="form-textarea"
              value={formData.description}
              onChange={handleChange}
              required
            ></textarea>
          </div>

          {/* Image Replace / Upload */}
          <div className="form-group">
            <label className="form-label">Item Photo</label>
            {imagePreview ? (
              <div className="preview-container">
                <img src={imagePreview} alt="New Preview" className="uploaded-preview-img" />
                <button type="button" onClick={removeImage} className="btn-remove-preview">
                  <X size={16} /> Discard New Photo
                </button>
              </div>
            ) : existingImageUrl ? (
              <div className="preview-container">
                <img src={existingImageUrl} alt="Current Photo" className="uploaded-preview-img" />
                <p className="form-hint mt-2">Current photo. Choose a file below to replace it:</p>
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleImageChange}
                  className="form-input mt-2"
                />
              </div>
            ) : (
              <div className="file-dropzone">
                <label className="dropzone-label">
                  <Upload size={32} className="dropzone-icon" />
                  <span className="dropzone-text">Click to choose a photo</span>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleImageChange}
                    className="sr-only"
                  />
                </label>
              </div>
            )}
          </div>

          <div className="form-actions mt-6">
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="btn btn-secondary"
              disabled={saving}
            >
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving Changes...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EditItemPage;
