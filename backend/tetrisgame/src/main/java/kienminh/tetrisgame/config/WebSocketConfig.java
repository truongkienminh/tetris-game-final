package kienminh.tetrisgame.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final kienminh.tetrisgame.config.WebSocketAuthChannelInterceptor authInterceptor;

    public WebSocketConfig(kienminh.tetrisgame.config.WebSocketAuthChannelInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ Register WebSocket endpoint with specific allowed origins
        registry.addEndpoint("/ws")
                // ✅ Specify your frontend URLs here (for development and production)
                .setAllowedOriginPatterns(
                        "http://localhost:5173",      // Vite dev server
                        "http://localhost:3000",      // Alternative dev port
                        "http://127.0.0.1:5173",
                        "http://127.0.0.1:3000"
                        // Add your production domain here: "https://yourdomain.com"
                )
                // ✅ Enable SockJS for better browser compatibility
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // ✅ Prefix for messages sent from client to server
        registry.setApplicationDestinationPrefixes("/app");

        // ✅ Prefix for messages sent from server to client
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        // ✅ Register JWT token validation interceptor
        registration.interceptors(authInterceptor);
    }
}