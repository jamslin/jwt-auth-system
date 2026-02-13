import React, { useState } from 'react';
import './Tester.css';

const Tester: React.FC = () => {
  const [isCollapsed, setIsCollapsed] = useState(false);

  const toggleCollapse = () => {
    setIsCollapsed(!isCollapsed);
  };

  return (
    <div className={`tester-container ${isCollapsed ? 'collapsed' : ''}`}>
      <div className="tester-header" onClick={toggleCollapse}>
        <h4>Tester</h4>
        <span className="collapse-icon">{isCollapsed ? '+' : '-'}</span>
      </div>
      {!isCollapsed && (
        <div className="tester-content">
          <p>Test Controls:</p>
          <button>Button 1</button>
          <button>Button 2</button>
          <button>Button 3</button>
        </div>
      )}
    </div>
  );
};

export default Tester;