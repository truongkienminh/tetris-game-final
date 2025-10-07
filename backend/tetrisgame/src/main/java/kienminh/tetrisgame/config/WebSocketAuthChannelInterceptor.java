package kienminh.tetrisgame.config;

import kienminh.tetrisgame.util.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // not used directly but keep for DI
    private final JwtUtil jwtUtil;

    public WebSocketAuthChannelInterceptor(JwtAuthenticationFilter jwtAuthenticationFilter, JwtUtil jwtUtil) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> auth = accessor.getNativeHeader("Authorization");
            if (auth != null && !auth.isEmpty()) {
                String header = auth.get(0);
                if (header.startsWith("Bearer ")) {
                    String token = header.substring(7);
                    try {
                        String username = jwtUtil.extractUsername(token);
                        if (jwtUtil.validateToken(token)) {
                            var userDetails = org.springframework.security.core.userdetails.User
                                    .withUsername(username)
                                    .password("")
                                    .authorities("USER")
                                    .build();
                            var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            accessor.setUser(authToken);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return message;
    }
}
