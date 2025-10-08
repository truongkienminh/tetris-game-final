package kienminh.tetrisgame.websocket;

import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.service.impl.RoomServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final RoomServiceImpl roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        var accessor = StompHeaderAccessor.wrap(event.getMessage());
        var auth = accessor.getUser();

        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            String username = token.getName();
            Room room = roomService.findRoomByUsername(username);
            if (room != null) {
                Player player = room.getPlayers().stream()
                        .filter(p -> p.getUser().getUsername().equals(username))
                        .findFirst().orElse(null);
                if (player != null) {
                    roomService.leaveRoom(room.getId(), player.getId());

                    WebSocketEvent wsEvent = WebSocketEvent.builder()
                            .type("LEAVE")
                            .roomId(room.getId())
                            .playerId(player.getId())
                            .username(username)
                            .build();

                    messagingTemplate.convertAndSend("/topic/room/" + room.getId(), wsEvent);
                }
            }
        }
    }
}
