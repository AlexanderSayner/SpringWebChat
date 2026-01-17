// GraphQL service for handling queries and mutations
class GraphQLService {
  constructor(webSocketUrl) {
    this.webSocketUrl = webSocketUrl;
    this.socket = null;
    this.subscriptions = new Map();
    this.requestId = 0;
  }

  // Connect to WebSocket
  connect(authToken) {
    return new Promise((resolve, reject) => {
      // For session-based authentication, we don't need to send an auth token
      // The authentication will be handled by session cookies
      this.socket = new WebSocket(this.webSocketUrl);

      this.socket.onopen = () => {
        console.log('Connected to GraphQL WebSocket');
        
        // Send connection initialization without auth token for session-based auth
        this.socket.send(JSON.stringify({
          type: 'connection_init',
          payload: {}
        }));
      };

      this.socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        
        switch (data.type) {
          case 'connection_ack':
            console.log('GraphQL WebSocket connection acknowledged');
            resolve();
            break;
            
          case 'data':
            // Handle subscription data
            const subscriptionCallback = this.subscriptions.get(data.id);
            if (subscriptionCallback) {
              subscriptionCallback(data.payload);
            }
            break;
            
          case 'error':
            console.error('GraphQL WebSocket error:', data.payload);
            break;
        }
      };

      this.socket.onerror = (error) => {
        console.error('WebSocket error:', error);
        reject(error);
      };

      this.socket.onclose = () => {
        console.log('GraphQL WebSocket connection closed');
      };
    });
  }

  // Execute a GraphQL query
  query(query, variables = {}) {
    return new Promise((resolve, reject) => {
      const id = (++this.requestId).toString();
      
      const message = {
        id,
        type: 'subscribe',
        payload: {
          query,
          variables
        }
      };
      
      this.socket.send(JSON.stringify(message));
      
      // Temporarily store the resolver to handle the response
      this.tempResolver = resolve;
    });
  }

  // Execute a GraphQL mutation
  mutate(mutation, variables = {}) {
    return this.query(mutation, variables);
  }

  // Subscribe to a GraphQL subscription
  subscribe(subscription, variables = {}, onNext, onError) {
    const id = (++this.requestId).toString();
    
    const message = {
      id,
      type: 'subscribe',
      payload: {
        query: subscription,
        variables
      }
    };
    
    this.socket.send(JSON.stringify(message));
    
    // Store subscription callback
    this.subscriptions.set(id, (payload) => {
      if (payload.errors) {
        if (onError) onError(payload.errors);
      } else {
        onNext(payload.data);
      }
    });
    
    return id;
  }

  // Unsubscribe from a GraphQL subscription
  unsubscribe(subscriptionId) {
    const message = {
      id: subscriptionId,
      type: 'complete'
    };
    
    this.socket.send(JSON.stringify(message));
    this.subscriptions.delete(subscriptionId);
  }

  // Disconnect from WebSocket
  disconnect() {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
  }
}

export default GraphQLService;