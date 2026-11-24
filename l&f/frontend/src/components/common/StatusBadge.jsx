import React from 'react';

const StatusBadge = ({ status }) => {
  const getBadgeConfig = () => {
    switch (status) {
      case 'LOST':
        return { label: 'Lost', className: 'badge badge-lost' };
      case 'FOUND':
        return { label: 'Found', className: 'badge badge-found' };
      case 'CLAIM_PENDING':
        return { label: 'Claim Pending', className: 'badge badge-pending' };
      case 'RECOVERED':
        return { label: 'Recovered', className: 'badge badge-recovered' };
      default:
        return { label: status, className: 'badge' };
    }
  };

  const { label, className } = getBadgeConfig();

  return <span className={className}>{label}</span>;
};

export default StatusBadge;
