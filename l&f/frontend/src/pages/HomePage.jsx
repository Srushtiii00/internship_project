import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { itemApi } from '../api/itemApi';
import { adminApi } from '../api/adminApi';
import ItemCard from '../components/items/ItemCard';
import { Search, PlusCircle, CheckCircle2, ShieldCheck, ArrowRight, Laptop, Briefcase, FileText, Key, Shirt, HelpCircle } from 'lucide-react';

const CATEGORIES = [
  { name: 'Electronics', icon: Laptop, color: '#3b82f6' },
  { name: 'Wallets & Bags', icon: Briefcase, color: '#8b5cf6' },
  { name: 'Documents', icon: FileText, color: '#f59e0b' },
  { name: 'Keys', icon: Key, color: '#ec4899' },
  { name: 'Clothing', icon: Shirt, color: '#10b981' },
  { name: 'Others', icon: HelpCircle, color: '#6b7280' },
];

const HomePage = () => {
  const [recentItems, setRecentItems] = useState([]);
  const [stats, setStats] = useState({
    totalItems: 0,
    totalLost: 0,
    totalFound: 0,
    totalRecovered: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [itemsRes, statsRes] = await Promise.allSettled([
          itemApi.getItems({ page: 0, size: 6 }),
          adminApi.getStats(),
        ]);

        if (itemsRes.status === 'fulfilled' && itemsRes.value?.data?.content) {
          setRecentItems(itemsRes.value.data.content);
        }
        if (statsRes.status === 'fulfilled' && statsRes.value?.data) {
          setStats(statsRes.value.data);
        }
      } catch (err) {
        console.error('Error fetching home data:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="container hero-container">
          <div className="hero-content">
            <span className="hero-pill">Community Lost & Found Portal</span>
            <h1 className="hero-title">
              Lost Something Important? <br />
              <span className="text-gradient">We Help You Find It.</span>
            </h1>
            <p className="hero-subtitle">
              A transparent, safe community space where you can report missing personal belongings,
              list found items, and safely verify ownership claims to bring items back home.
            </p>

            <div className="hero-actions">
              <Link to="/report" className="btn btn-primary btn-lg">
                <PlusCircle size={20} />
                <span>Report an Item</span>
              </Link>
              <Link to="/browse" className="btn btn-secondary btn-lg">
                <Search size={20} />
                <span>Search Listings</span>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Stats Counter Section */}
      <section className="stats-strip">
        <div className="container stats-grid">
          <div className="stat-card">
            <span className="stat-value">{stats.totalItems || 0}</span>
            <span className="stat-label">Total Listings</span>
          </div>
          <div className="stat-card">
            <span className="stat-value text-red">{stats.totalLost || 0}</span>
            <span className="stat-label">Lost Reports</span>
          </div>
          <div className="stat-card">
            <span className="stat-value text-blue">{stats.totalFound || 0}</span>
            <span className="stat-label">Found Reports</span>
          </div>
          <div className="stat-card">
            <span className="stat-value text-green">{stats.totalRecovered || 0}</span>
            <span className="stat-label">Items Recovered</span>
          </div>
        </div>
      </section>

      {/* Categories Grid */}
      <section className="section categories-section">
        <div className="container">
          <div className="section-header">
            <div>
              <h2 className="section-title">Explore by Category</h2>
              <p className="section-subtitle">Quickly filter through items by common categories</p>
            </div>
            <Link to="/browse" className="section-link">
              Browse All <ArrowRight size={16} />
            </Link>
          </div>

          <div className="categories-grid">
            {CATEGORIES.map((cat) => {
              const IconComponent = cat.icon;
              return (
                <Link
                  key={cat.name}
                  to={`/browse?category=${encodeURIComponent(cat.name)}`}
                  className="category-card"
                >
                  <div className="category-icon-wrapper" style={{ backgroundColor: `${cat.color}15`, color: cat.color }}>
                    <IconComponent size={28} />
                  </div>
                  <span className="category-name">{cat.name}</span>
                </Link>
              );
            })}
          </div>
        </div>
      </section>

      {/* Recent Items Section */}
      <section className="section recent-items-section">
        <div className="container">
          <div className="section-header">
            <div>
              <h2 className="section-title">Recently Reported Items</h2>
              <p className="section-subtitle">Check the latest lost and found items in your community</p>
            </div>
            <Link to="/browse" className="section-link">
              View All Listings <ArrowRight size={16} />
            </Link>
          </div>

          {loading ? (
            <div className="flex-center py-8">
              <div className="spinner"></div>
            </div>
          ) : recentItems.length > 0 ? (
            <div className="items-grid">
              {recentItems.map((item) => (
                <ItemCard key={item.id} item={item} />
              ))}
            </div>
          ) : (
            <div className="empty-state-box">
              <Search size={48} className="text-muted" />
              <h3>No items reported yet</h3>
              <p>Be the first to report a lost or found item to help our community!</p>
              <Link to="/report" className="btn btn-primary mt-4">
                Report an Item Now
              </Link>
            </div>
          )}
        </div>
      </section>

      {/* How It Works Section */}
      <section className="section how-it-works-section">
        <div className="container">
          <div className="text-center mb-8">
            <h2 className="section-title">How It Works</h2>
            <p className="section-subtitle">Simple 3-step process to recover or return items</p>
          </div>

          <div className="how-it-works-grid">
            <div className="step-card">
              <div className="step-number">1</div>
              <h3 className="step-title">Report an Item</h3>
              <p className="step-desc">
                Provide key details, date, exact location, and optional photo so others can identify it.
              </p>
            </div>

            <div className="step-card">
              <div className="step-number">2</div>
              <h3 className="step-title">Search & Verify</h3>
              <p className="step-desc">
                Search through listings using filters. If you spot your item, submit an ownership claim with proof.
              </p>
            </div>

            <div className="step-card">
              <div className="step-number">3</div>
              <h3 className="step-title">Reconnect & Return</h3>
              <p className="step-desc">
                The finder reviews your proof and approves the claim. Safe return is coordinated directly.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default HomePage;
