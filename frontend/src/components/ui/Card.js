import React from 'react';

const Card = ({ 
  children, 
  className = '', 
  title, 
  subtitle,
  ...props 
}) => {
  return (
    <div className={`bg-white rounded-xl shadow-lg border border-gray-100 ${className}`} {...props}>
      {(title || subtitle) && (
        <div className="px-6 py-4 border-b border-gray-100">
          {title && (
            <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
          )}
          {subtitle && (
            <p className="text-sm text-gray-500 mt-1">{subtitle}</p>
          )}
        </div>
      )}
      <div className="px-6 py-4">
        {children}
      </div>
    </div>
  );
};

export default Card;
