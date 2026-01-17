// Runtime configuration service
// Loads configuration from environment variables or a config endpoint

// Helper functions to construct URLs from BACKEND_HOST
export function getWebSocketUrl() {
  const backendHost = getConfig('BACKEND_HOST');
  // Convert HTTP/HTTPS to WS/WSS
  if (backendHost.startsWith('https://')) {
    return backendHost.replace('https://', 'wss://') + '/graphql';
  } else {
    return backendHost.replace('http://', 'ws://') + '/graphql';
  }
}

export function getHttpUrl(path = '') {
  const backendHost = getConfig('BACKEND_HOST');
  return backendHost + path;
}

// Default configuration values
const DEFAULT_CONFIG = {
  BACKEND_HOST: 'http://localhost:8080',
  VK_APP_ID: '12345678', // Replace with your actual VK app ID
};

// Function to get configuration value
export function getConfig(key) {
  // Check for browser window object (client-side)
  if (typeof window !== 'undefined' && window._APP_CONFIG) {
    return window._APP_CONFIG[key] || DEFAULT_CONFIG[key];
  }
  
  // Fallback to environment variable (for dev mode)
  return import.meta.env[`VITE_${key}`] || DEFAULT_CONFIG[key];
}

// Function to initialize config from global variable or fetch
export async function initializeConfig() {
  if (typeof window !== 'undefined') {
    // If config is already loaded in a global variable, use it
    if (window._APP_CONFIG) {
      return window._APP_CONFIG;
    }
    
    // Otherwise, try to fetch config from a config endpoint
    try {
      const response = await fetch('/api/config');
      if (response.ok) {
        const config = await response.json();
        window._APP_CONFIG = { ...DEFAULT_CONFIG, ...config };
        return window._APP_CONFIG;
      }
    } catch (error) {
      console.warn('Could not load config from API, using defaults:', error);
    }
    
    // Set default config in global variable
    window._APP_CONFIG = DEFAULT_CONFIG;
    return DEFAULT_CONFIG;
  }
  
  // For server-side or fallback
  return DEFAULT_CONFIG;
}