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
        registry.addEndpoint("/ws") // endpoint frontend sẽ connect
                .setAllowedOrigins("*") // hoặc "http://localhost:5173"
                .withSockJS(); // fallback cho trình duyệt cũ
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // prefix cho client gửi lên server
        registry.setApplicationDestinationPrefixes("/app");
        // prefix cho server gửi xuống client
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}
