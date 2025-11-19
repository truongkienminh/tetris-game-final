package kienminh.tetrisgame.config;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import kienminh.tetrisgame.util.JwtUtil;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public WebSocketAuthChannelInterceptor(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // ✅ Only process CONNECT command
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            // ✅ Extract JWT token from header
            String token = accessor.getFirstNativeHeader("token");

            if (token == null || token.isBlank()) {
                // ✅ Set as anonymous if no token provided
                accessor.setUser(new AnonymousAuthenticationToken(
                        "anon", "guest",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                ));
                return message;
            }

            try {
                // ✅ Validate and authenticate token
                String username = jwtUtil.extractUsername(token);
                if (jwtUtil.validateToken(token)) {

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    accessor.setUser(auth);
                }
            } catch (Exception e) {
                // ✅ Log and set as anonymous on error
                System.out.println("WebSocket authentication error: " + e.getMessage());
                accessor.setUser(new AnonymousAuthenticationToken(
                        "anon", "guest",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                ));
            }
        }

        return message;
    }
}