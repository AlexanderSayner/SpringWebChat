# Web Chat Frontend

A Svelte-based web chat frontend that connects to a backend via WebSockets, implements GraphQL queries/mutations/subscriptions, and provides VK OAuth authorization.

## Features

- Real-time chat using WebSocket connections
- GraphQL integration for data queries and mutations
- VK OAuth for user authentication
- Responsive design for various screen sizes
- Configurable backend connection via environment variables

## Project Structure

```
web-chat-frontend/
├── src/
│   ├── lib/
│   │   ├── ChatComponent.svelte     # Main chat UI component
│   │   └── graphql-service.js       # GraphQL WebSocket service
│   ├── routes/
│   │   └── vk-callback/
│   │       └── +page.svelte         # VK OAuth callback handler
│   ├── App.svelte                   # Main application component
│   └── main.js                      # Application entry point
├── public/                          # Static assets
├── package.json                     # Dependencies and scripts
└── vite.config.js                   # Vite configuration
```

## Setup Instructions

1. Install dependencies:
```bash
npm install
```

2. Configure your backend host using environment variables:
```bash
# Create a .env file with your configuration
cp .env.example .env
# Edit .env to set your desired VITE_BACKEND_HOST
```

Alternatively, the connection URL can be configured at runtime when using Docker (see below).

3. Set up your VK OAuth application and replace `YOUR_VK_APP_ID` in the VK login function with your actual VK application ID.

4. Start the development server:
```bash
npm run dev
```

## Configuration Options

### Environment Variables

The application supports the following environment variables:

**Development/Build Time:**
- `VITE_BACKEND_HOST`: Sets the backend host URL for constructing all backend endpoints during development/build time (e.g., http://localhost:8180)

**Runtime (Docker):**
- `BACKEND_HOST`: Sets the backend host URL for constructing all backend endpoints when starting the container

### VK OAuth Setup
To use VK OAuth:
1. Create a VK application at https://vk.com/apps
2. Set the redirect URI to your domain (e.g., http://localhost:5173/vk-callback)
3. Replace `YOUR_VK_APP_ID` in `ChatComponent.svelte` with your actual VK app ID

### Backend Integration
This frontend expects a backend that supports:
- WebSocket connections for GraphQL subscriptions
- GraphQL mutations for sending messages
- JWT token authentication via Authorization header

## Running with Docker Compose

To run the entire application stack (frontend, backend, database):

```bash
cd /workspace
docker-compose up --build
```

The frontend will be available at `http://localhost:3000` and will automatically connect to the backend service using the `BACKEND_HOST` environment variable.

## Running the Development Server

```bash
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) to view the application in your browser.

## Building for Production

```bash
npm run build
```

This will create a production-ready build in the `dist/` directory.

## Key Components

### ChatComponent.svelte
Main chat interface with:
- WebSocket connection to backend using configurable URL
- Message sending/receiving functionality
- User authentication status
- Connection status indicator

### graphql-service.js
Handles all GraphQL communication via WebSockets:
- Connection initialization
- Query execution
- Subscription management
- Error handling

### VK OAuth Flow
The VK OAuth implementation follows these steps:
1. Opens a popup window for VK authentication
2. VK redirects to the callback endpoint with access token
3. Callback page retrieves user information from VK API
4. Sends authentication success message to parent window
5. Parent window receives credentials and establishes WebSocket connection
