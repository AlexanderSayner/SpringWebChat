<script>
  import { onMount, onDestroy } from 'svelte';
  import GraphQLService from '../lib/graphql-service.js';
  import { initializeConfig, getConfig, getWebSocketUrl, getHttpUrl } from '../lib/config.js';
  
  // State
  let graphQLService = null;
  let messages = [];
  let newMessage = '';
  let isConnected = false;
  let username = '';
  let subscriptionId = null;
  let isAuthenticated = false;
  
  // Check authentication status on mount
  onMount(async () => {
    // First check if user is already authenticated
    checkAuthentication();
  });
  
  // Function to check if user is authenticated
  async function checkAuthentication() {
    try {
      const apiUrl = getHttpUrl('/api/user');
      const response = await fetch(apiUrl);
      if (response.ok) {
        const userData = await response.json();
        if (userData.authenticated) {
          isAuthenticated = true;
          username = userData.displayName || userData.name || 'User';
          
          // Initialize GraphQL connection
          initializeGraphQLConnection();
        } else {
          isAuthenticated = false;
        }
      } else {
        isAuthenticated = false;
      }
    } catch (error) {
      console.error('Error checking authentication:', error);
      isAuthenticated = false;
    }
  }
  
  // Function to initialize GraphQL connection
  async function initializeGraphQLConnection() {
    if (!isAuthenticated) return;
    
    const backendWsUrl = getWebSocketUrl();
    
    // Create GraphQL service instance
    graphQLService = new GraphQLService(backendWsUrl);
    
    try {
      // We need to pass some form of authentication - for now using a placeholder
      // In a real scenario, you might use cookies or headers
      await graphQLService.connect(null); // Using null as we rely on session cookies
      isConnected = true;
      
      // Subscribe to new messages
      subscriptionId = graphQLService.subscribe(
        `subscription {
          messageAdded {
            id
            content
            sender
            timestamp
          }
        }`,
        {},
        (data) => {
          // Handle incoming messages
          messages = [...messages, data.messageAdded];
        },
        (errors) => {
          console.error('Subscription error:', errors);
        }
      );
    } catch (error) {
      console.error('Failed to connect to GraphQL WebSocket:', error);
    }
  }
  
  // Function to handle VK OAuth login via backend
  async function vkLogin() {
    // Redirect to backend's OAuth2 endpoint
    const oauthUrl = getHttpUrl('/oauth2/authorization/vk');
    window.location.href = oauthUrl;
  }
  
  // Function to send a message
  async function sendMessage() {
    if (!newMessage.trim() || !graphQLService || !isAuthenticated) return;
    
    try {
      await graphQLService.mutate(
        `mutation SendMessage($content: String!, $sender: String!) {
          sendMessage(content: $content, sender: $sender) {
            id
            content
            sender
            timestamp
          }
        }`,
        {
          content: newMessage,
          sender: username
        }
      );
      newMessage = '';
    } catch (error) {
      console.error('Failed to send message:', error);
    }
  }
  
  // Function to handle logout
  async function logout() {
    try {
      const logoutUrl = getHttpUrl('/logout');
      await fetch(logoutUrl, {
        method: 'POST',
        credentials: 'include' // Important to include session cookies
      });
      
      // Reset state
      isAuthenticated = false;
      username = '';
      isConnected = false;
      
      if (graphQLService) {
        graphQLService.disconnect();
        graphQLService = null;
      }
      
      messages = [];
      newMessage = '';
    } catch (error) {
      console.error('Logout failed:', error);
    }
  }
  
  // Cleanup on component destroy
  onDestroy(() => {
    if (subscriptionId) {
      graphQLService.unsubscribe(subscriptionId);
    }
    if (graphQLService) {
      graphQLService.disconnect();
    }
  });
</script>

<div class="chat-container">
  <header class="chat-header">
    <h2>Web Chat</h2>
    {#if !isAuthenticated}
      <button class="login-btn" on:click={vkLogin}>
        Login with VK
      </button>
    {:else}
      <div class="user-controls">
        <span class="user-info">Welcome, {username}!</span>
        <button class="logout-btn" on:click={logout}>Logout</button>
      </div>
    {/if}
  </header>
  
  <div class="status-bar">
    <span class={isConnected ? 'connected' : 'disconnected'}>
      {isConnected ? '● Connected' : '○ Disconnected'}
    </span>
  </div>
  
  <div class="messages-container">
    {#each messages as message (message.id)}
      <div class="message">
        <strong>{message.sender}:</strong>
        <p>{message.content}</p>
        <small>{new Date(message.timestamp).toLocaleString()}</small>
      </div>
    {:else}
      <div class="no-messages">No messages yet...</div>
    {/each}
  </div>
  
  {#if isAuthenticated && isConnected}
    <form class="message-form" on:submit|preventDefault={sendMessage}>
      <input
        type="text"
        bind:value={newMessage}
        placeholder="Type your message..."
        required
      />
      <button type="submit" disabled={!newMessage.trim()}>
        Send
      </button>
    </form>
  {/if}
</div>

<style>
  .chat-container {
    max-width: 800px;
    margin: 0 auto;
    border: 1px solid #ddd;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  }
  
  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    background-color: #f5f5f5;
    border-bottom: 1px solid #ddd;
  }
  
  .login-btn {
    background-color: #4a76a8;
    color: white;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    cursor: pointer;
  }
  
  .login-btn:hover {
    background-color: #3a5a78;
  }
  
  .logout-btn {
    background-color: #dc3545;
    color: white;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    cursor: pointer;
    margin-left: 1rem;
  }
  
  .logout-btn:hover {
    background-color: #c82333;
  }
  
  .user-controls {
    display: flex;
    align-items: center;
  }
  
  .status-bar {
    padding: 0.5rem 1rem;
    background-color: #f9f9f9;
    font-size: 0.9rem;
  }
  
  .connected {
    color: green;
  }
  
  .disconnected {
    color: red;
  }
  
  .messages-container {
    height: 400px;
    overflow-y: auto;
    padding: 1rem;
    background-color: #fafafa;
  }
  
  .message {
    margin-bottom: 1rem;
    padding: 0.75rem;
    background-color: white;
    border-radius: 4px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  }
  
  .message p {
    margin: 0.5rem 0 0.25rem;
  }
  
  .message small {
    color: #666;
    font-size: 0.8rem;
  }
  
  .no-messages {
    text-align: center;
    color: #666;
    font-style: italic;
    padding-top: 50%;
  }
  
  .message-form {
    display: flex;
    padding: 1rem;
    background-color: white;
    border-top: 1px solid #ddd;
  }
  
  .message-form input {
    flex: 1;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px 0 0 4px;
    font-size: 1rem;
  }
  
  .message-form button {
    padding: 0.75rem 1rem;
    background-color: #4a76a8;
    color: white;
    border: none;
    border-radius: 0 4px 4px 0;
    cursor: pointer;
  }
  
  .message-form button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
  }
</style>