import React from 'react';

const Placeholder = ({ title }) => {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
      <div className="card glass-panel" style={{ textAlign: 'center', padding: '4rem' }}>
        <h2 style={{ color: 'var(--primary-light)', marginBottom: '1rem' }}>{title}</h2>
        <p style={{ color: 'var(--text-muted)' }}>This feature is currently under development.</p>
        <p style={{ color: 'var(--text-muted)' }}>Check back soon for updates!</p>
      </div>
    </div>
  );
};

export default Placeholder;
