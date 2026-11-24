import React from 'react';
import { Search, RotateCcw, Filter } from 'lucide-react';

const CATEGORIES = [
  'All Categories',
  'Electronics',
  'Wallets & Bags',
  'Documents',
  'Keys',
  'Clothing',
  'Jewelry & Accessories',
  'Books & Stationery',
  'Others',
];

const ItemFilterBar = ({ filters, onFilterChange, onReset }) => {
  return (
    <div className="filter-bar-card">
      <div className="filter-row-top">
        {/* Search Input */}
        <div className="search-input-wrapper">
          <Search size={18} className="search-icon" />
          <input
            type="text"
            placeholder="Search by title, description, or location..."
            value={filters.keyword || ''}
            onChange={(e) => onFilterChange('keyword', e.target.value)}
            className="filter-search-input"
          />
        </div>

        {/* Type Toggle Pills */}
        <div className="type-toggle-group">
          <button
            type="button"
            className={`type-pill ${!filters.type ? 'active' : ''}`}
            onClick={() => onFilterChange('type', '')}
          >
            All
          </button>
          <button
            type="button"
            className={`type-pill pill-lost-tab ${filters.type === 'LOST' ? 'active' : ''}`}
            onClick={() => onFilterChange('type', 'LOST')}
          >
            Lost
          </button>
          <button
            type="button"
            className={`type-pill pill-found-tab ${filters.type === 'FOUND' ? 'active' : ''}`}
            onClick={() => onFilterChange('type', 'FOUND')}
          >
            Found
          </button>
        </div>
      </div>

      <div className="filter-row-bottom">
        {/* Category Select */}
        <div className="filter-select-wrapper">
          <label className="filter-label">Category</label>
          <select
            value={filters.category || 'All Categories'}
            onChange={(e) =>
              onFilterChange('category', e.target.value === 'All Categories' ? '' : e.target.value)
            }
            className="filter-select"
          >
            {CATEGORIES.map((cat) => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>
        </div>

        {/* Status Select */}
        <div className="filter-select-wrapper">
          <label className="filter-label">Status</label>
          <select
            value={filters.status || ''}
            onChange={(e) => onFilterChange('status', e.target.value)}
            className="filter-select"
          >
            <option value="">All Statuses</option>
            <option value="LOST">Lost</option>
            <option value="FOUND">Found</option>
            <option value="CLAIM_PENDING">Claim Pending</option>
            <option value="RECOVERED">Recovered</option>
          </select>
        </div>

        {/* Reset Button */}
        <div className="filter-action-wrapper">
          <button type="button" onClick={onReset} className="btn btn-secondary filter-reset-btn">
            <RotateCcw size={15} />
            <span>Reset</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default ItemFilterBar;
