import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { itemApi } from '../api/itemApi';
import ItemCard from '../components/items/ItemCard';
import ItemFilterBar from '../components/items/ItemFilterBar';
import { PackageOpen, ChevronLeft, ChevronRight } from 'lucide-react';

const BrowseItemsPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const [filters, setFilters] = useState({
    keyword: searchParams.get('keyword') || '',
    category: searchParams.get('category') || '',
    type: searchParams.get('type') || '',
    status: searchParams.get('status') || '',
  });

  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  // Sync state if URL query params change
  useEffect(() => {
    setFilters({
      keyword: searchParams.get('keyword') || '',
      category: searchParams.get('category') || '',
      type: searchParams.get('type') || '',
      status: searchParams.get('status') || '',
    });
  }, [searchParams]);

  const fetchItems = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 9,
      };
      if (filters.keyword) params.keyword = filters.keyword;
      if (filters.category && filters.category !== 'All Categories') params.category = filters.category;
      if (filters.type) params.type = filters.type;
      if (filters.status) params.status = filters.status;

      const res = await itemApi.getItems(params);
      if (res.success && res.data) {
        setItems(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
        setTotalElements(res.data.totalElements || 0);
      }
    } catch (err) {
      console.error('Failed to fetch items:', err);
    } finally {
      setLoading(false);
    }
  }, [filters, page]);

  useEffect(() => {
    fetchItems();
  }, [fetchItems]);

  const handleFilterChange = (key, value) => {
    setPage(0);
    const newFilters = { ...filters, [key]: value };
    setFilters(newFilters);

    // Update query params in URL
    const nextParams = {};
    Object.entries(newFilters).forEach(([k, v]) => {
      if (v) nextParams[k] = v;
    });
    setSearchParams(nextParams);
  };

  const handleResetFilters = () => {
    setPage(0);
    const reset = { keyword: '', category: '', type: '', status: '' };
    setFilters(reset);
    setSearchParams({});
  };

  return (
    <div className="container browse-page py-8">
      <div className="page-header mb-6">
        <h1 className="page-title">Browse Lost & Found Listings</h1>
        <p className="page-subtitle">
          Showing {totalElements} {totalElements === 1 ? 'item' : 'items'} found in the database
        </p>
      </div>

      {/* Filter Bar Component */}
      <ItemFilterBar
        filters={filters}
        onFilterChange={handleFilterChange}
        onReset={handleResetFilters}
      />

      {/* Items Section */}
      {loading ? (
        <div className="flex-center py-12">
          <div className="spinner"></div>
        </div>
      ) : items.length > 0 ? (
        <>
          <div className="items-grid mt-6">
            {items.map((item) => (
              <ItemCard key={item.id} item={item} />
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination-wrapper mt-8">
              <button
                className="btn btn-secondary pagination-btn"
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                <ChevronLeft size={16} /> Previous
              </button>

              <span className="pagination-info">
                Page {page + 1} of {totalPages}
              </span>

              <button
                className="btn btn-secondary pagination-btn"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                Next <ChevronRight size={16} />
              </button>
            </div>
          )}
        </>
      ) : (
        <div className="empty-state-box mt-6">
          <PackageOpen size={56} className="text-muted" />
          <h3>No matching items found</h3>
          <p>Try adjusting your search terms or clearing your category and status filters.</p>
          <button onClick={handleResetFilters} className="btn btn-secondary mt-4">
            Reset Filters
          </button>
        </div>
      )}
    </div>
  );
};

export default BrowseItemsPage;
