# Web Chat Backend - GraphQL API Documentation

## Table of Contents
1. [Overview](#overview)
2. [Authentication](#authentication)
3. [GraphQL Queries](#graphql-queries)
4. [GraphQL Mutations](#graphql-mutations)
5. [Examples](#examples)

## Overview

This document provides information about the GraphQL API endpoints available in the web chat backend and how authentication works in the system.

## Authentication

The application implements OAuth2 authentication with VK (VKontakte) as the identity provider. Here's how the authentication system works:

### OAuth2 Flow
1. **Configuration**: The application is configured in `application.yaml` with VK OAuth2 settings:
   - Client ID and Secret are loaded from environment variables (`VK_CLIENT_ID`, `VK_CLIENT_SECRET`)
   - Authorization URI: `https://oauth.vk.com/authorize`
   - Token URI: `https://oauth.vk.com/access_token`
   - User Info URI: `https://api.vk.com/method/users.get?fields=photo_200`

2. **Security Configuration**: 
   - The `SecurityConfig.java` class configures Spring Security
   - Public endpoints include: `/`, `/ws/**`, `/graphql`, `/graphiql`, `/oauth2/**`, `/login/**`
   - All other endpoints require authentication
   - CSRF protection is disabled (typical for API applications)
   - CORS is configured to allow all origins and methods

3. **User Management**:
   - Upon successful OAuth2 login, user information is retrieved from VK API
   - Users are stored in the database with fields: `vkId`, `username`, `displayName`, `avatarUrl`
   - The system maps VK user IDs to internal user accounts

4. **Current Implementation Status**:
   - The GraphQL API endpoints are currently accessible without authentication (as seen in SecurityConfig)
   - WebSocket connections use anonymous users in the current implementation
   - A proper user context extraction mechanism would be needed for production use

## GraphQL Queries

### `messages(count: Int)` 

Fetches the latest chat messages.

**Arguments:**
- `count` (Int): Number of messages to fetch (optional, defaults to 50)

**Returns:** Array of `Message` objects

**Example Response:**
```json
[
  {
    "id": "1",
    "content": "Hello world!",
    "createdAt": "2023-10-15T10:30:00Z",
    "user": {
      "id": "1",
      "username": "john_doe",
      "displayName": "John Doe",
      "avatarUrl": "https://example.com/avatar.jpg"
    }
  }
]
```

### `users()`

Fetches all registered users.

**Returns:** Array of `User` objects

**Example Response:**
```json
[
  {
    "id": "1",
    "vkId": "123456789",
    "username": "john_doe",
    "displayName": "John Doe",
    "avatarUrl": "https://example.com/avatar.jpg",
    "createdAt": "2023-10-15T10:00:00Z"
  }
]
```

## GraphQL Mutations

### `sendMessage(input: MessageInput!)`

Sends a new message to the chat.

**Arguments:**
- `input` (MessageInput!): Input object containing message data

**Input Type:**
- `content` (String!): The message content

**Returns:** `Message` object

**Example Response:**
```json
{
  "id": "2",
  "content": "New message",
  "createdAt": "2023-10-15T11:00:00Z",
  "user": {
    "id": "1",
    "username": "john_doe",
    "displayName": "John Doe",
    "avatarUrl": "https://example.com/avatar.jpg"
  }
}
```

## Examples

### Query Examples

#### Get latest 10 messages:
```graphql
query GetMessages {
  messages(count: 10) {
    id
    content
    createdAt
    user {
      id
      username
      displayName
      avatarUrl
    }
  }
}
```

#### Get all users:
```graphql
query GetUsers {
  users {
    id
    username
    displayName
    avatarUrl
    vkId
    createdAt
  }
}
```

#### Get messages and users together:
```graphql
query GetChatData {
  messages(count: 20) {
    id
    content
    createdAt
    user {
      id
      username
      displayName
    }
  }
  users {
    id
    username
    displayName
    avatarUrl
  }
}
```

### Mutation Examples

#### Send a new message:
```graphql
mutation SendMessage {
  sendMessage(input: { content: "Hello from GraphQL!" }) {
    id
    content
    createdAt
    user {
      id
      username
    }
  }
}
```

## Accessing the GraphQL Interface

The GraphQL endpoint is available at:
- **API Endpoint**: `http://localhost:8180/graphql`
- **GraphiQL UI**: `http://localhost:8180/graphiql` (for development/testing)

## Notes

- The current implementation includes placeholder logic for user authentication in WebSocket connections
- Production implementations should extract authenticated user context from security context in GraphQL resolvers
- The application uses PostgreSQL database with Flyway for migrations
- CORS is configured permissively for development purposes - this should be restricted in production

## Troubleshooting Common Issues

### VK OAuth Login Error: "application was deleted"

If you encounter the error `{"error":"invalid_request","error_description":"application was deleted"}` when clicking the "Login with VK" button, this indicates that the VK application ID is not properly configured.

#### Solution:

1. **Register your application on VK**:
   - Go to https://vk.com/apps
   - Click "Create Application"
   - Choose "Website" as the platform
   - Enter your site domain (for local development, use http://localhost:5173 or http://localhost:3000 depending on your setup)
   - Note down the Application ID that gets generated

2. **Configure the VK App ID**:
   - Update the `VK_APP_ID` in your environment variables
   - Or modify the default value in `/workspace/web-chat-frontend/src/lib/config.js`
   - Also update the value in `/workspace/web-chat-frontend/runtime-config.js`

3. **Environment Variable Setup**:
   - Copy the `.env.example` file to `.env` in the frontend directory
   - Set `VITE_VK_APP_ID` to your actual VK application ID

Example `.env` file:
```
VITE_BACKEND_WS_URL=ws://localhost:8080/graphql
VITE_VK_APP_ID=5195019  # Replace with your actual VK app ID
```

4. **VK App Settings**:
   - Make sure your VK app has the correct redirect URI configured
   - For local development, this would typically be `http://localhost:5173/vk-callback` or `http://localhost:3000/vk-callback`

After making these changes, restart your application for the new configuration to take effect.