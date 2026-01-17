// Runtime configuration script
// This script will be executed when the container starts to inject config

function generateRuntimeConfig() {
  // Get environment variables
  const backendHost = process.env.BACKEND_HOST || process.env.VITE_BACKEND_HOST || 'http://localhost:8080';
  const vkAppId = process.env.VK_APP_ID || '12345678'; // Replace with your actual VK app ID
  
  // Generate JavaScript that sets the global config
  const configJs = `
    window._APP_CONFIG = {
      BACKEND_HOST: '${backendHost}',
      VK_APP_ID: '${vkAppId}'
    };
  `;
  
  return configJs;
}

// If running in Node.js environment (for server-side generation)
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { generateRuntimeConfig };
}