<script>
  import { onMount } from 'svelte';
  
  onMount(() => {
    // Extract token from URL hash
    const hashParams = new URLSearchParams(window.location.hash.substring(1));
    const accessToken = hashParams.get('access_token');
    const email = hashParams.get('email');
    
    if (accessToken) {
      // Get user info from VK API
      fetch(`https://api.vk.com/method/users.get?access_token=${accessToken}&v=5.131`)
        .then(response => response.json())
        .then(data => {
          if (data.response && data.response.length > 0) {
            const user = data.response[0];
            
            // Send authentication success message to parent window
            window.opener.postMessage({
              type: 'VK_AUTH_SUCCESS',
              token: accessToken,
              username: `${user.first_name} ${user.last_name}`,
              email: email
            }, window.location.origin);
            
            // Close the popup window
            window.close();
          }
        })
        .catch(error => {
          console.error('Error fetching user info:', error);
          window.close();
        });
    } else {
      console.error('Access token not found in URL hash');
      window.close();
    }
  });
</script>

<p>Processing VK authentication...</p>