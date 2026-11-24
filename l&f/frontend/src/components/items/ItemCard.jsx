import React from 'react';
import { Link } from 'react-router-dom';
import StatusBadge from '../common/StatusBadge';
import { MapPin, Calendar, Tag, Image as ImageIcon } from 'lucide-react';
import { API_BASE_URL } from '../../api/axiosClient';

const ItemCard = ({ item }) => {
  const imageUrl = item.imageUrl
    ? (item.imageUrl.startsWith('http') ? item.imageUrl : `${API_BASE_URL}${item.imageUrl}`)
    : null;

  return (
    <div className="item-card">
      <Link to={`/items/${item.id}`} className="card-image-link">
        <div className="card-image-container">
          {imageUrl ? (
            <img src={imageUrl} alt={item.title} className="card-image" loading="lazy" />
          ) : (
            <div className="card-image-placeholder">
              <ImageIcon size={40} className="text-muted" />
              <span className="placeholder-text">No image provided</span>
            </div>
          )}
          <div className="card-badge-overlay">
            <span className={`pill-type ${item.type === 'LOST' ? 'type-lost' : 'type-found'}`}>
              {item.type}
            </span>
            <StatusBadge status={item.status} />
          </div>
        </div>
      </Link>

      <div className="card-body">
        <div className="card-meta-top">
          <span className="category-tag">
            <Tag size={13} /> {item.category}
          </span>
        </div>

        <h3 className="card-title">
          <Link to={`/items/${item.id}`} title={item.title}>
            {item.title}
          </Link>
        </h3>

        <p className="card-desc">
          {item.description?.length > 90
            ? `${item.description.substring(0, 90)}...`
            : item.description}
        </p>

        <div className="card-details">
          <div className="detail-row">
            <MapPin size={15} className="detail-icon" />
            <span className="detail-text" title={item.location}>{item.location}</span>
          </div>
          <div className="detail-row">
            <Calendar size={15} className="detail-icon" />
            <span className="detail-text">{item.itemDate}</span>
          </div>
        </div>

        <div className="card-footer">
          <Link to={`/items/${item.id}`} className="btn-card-action">
            View Details &rarr;
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ItemCard;
