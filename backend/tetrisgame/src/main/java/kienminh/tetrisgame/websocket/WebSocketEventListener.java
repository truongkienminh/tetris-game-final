package kienminh.tetrisgame.websocket;

import kienminh.tetrisgame.dto.PlayerDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.service.interfaces.RoomService;
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

    private final RoomService roomService;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        var accessor = StompHeaderAccessor.wrap(event.getMessage());
        var auth = accessor.getUser();

        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            String username = token.getName();

            // 🔹 Tìm player theo username
            Player player = playerRepository.findByUser_Username(username)
                    .orElse(null);

            if (player != null && player.getRoom() != null) {
                Room room = player.getRoom();

                // 🔹 Xử lý rời phòng qua service (service sẽ cập nhật DB và xóa phòng nếu trống)
                roomService.leaveRoom(room.getId(), player);

                // 🔹 Gửi sự kiện LEAVE qua WebSocket
                WebSocketEvent wsEvent = WebSocketEvent.builder()
                        .type("LEAVE")
                        .roomId(room.getId())
                        .playerId(player.getId())
                        .username(username)
                        .payload(new PlayerDTO(player))
                        .build();

                messagingTemplate.convertAndSend("/topic/room/" + room.getId(), wsEvent);
            }
        }
    }
}
