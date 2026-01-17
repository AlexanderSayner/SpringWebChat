# Use Node.js base image
FROM node:20-alpine

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Build the Svelte application
RUN npm run build

# Install a simple HTTP server globally
RUN npm install -g serve

# Create a startup script to inject environment variables at runtime
RUN echo '#!/bin/sh\n\
echo "/* Generated runtime config */" > /app/dist/config.js\n\
echo "window._APP_CONFIG = {" >> /app/dist/config.js\n\
echo "  BACKEND_HOST: \"${BACKEND_HOST:-http://localhost:8080}\"," >> /app/dist/config.js\n\
echo "  VK_APP_ID: \"${VK_APP_ID:-5195019}\"" >> /app/dist/config.js\n\
echo "};" >> /app/dist/config.js\n\
echo "Starting server with BACKEND_HOST=$BACKEND_HOST VK_APP_ID=$VK_APP_ID"\n\
exec serve -s dist "$@"' > /app/start.sh && chmod +x /app/start.sh

# Expose port
EXPOSE 3000

# Start the application
CMD ["/app/start.sh"]