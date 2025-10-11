package kienminh.tetrisgame.service.impl;

import kienminh.tetrisgame.dto.PlayerDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.repository.RoomRepository;
import kienminh.tetrisgame.service.interfaces.RoomService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Quản lý game loop từng phòng
    private final Map<Long, ScheduledFuture<?>> gameLoops = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public RoomServiceImpl(RoomRepository roomRepository,
                           PlayerRepository playerRepository,
                           SimpMessagingTemplate messagingTemplate) {
        this.roomRepository = roomRepository;
        this.playerRepository = playerRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ===========================
    // CREATE / JOIN / LEAVE ROOM
    // ===========================
    @Override
    public Room createRoom(User host) {
        Room room = Room.builder()
                .name(host.getUsername() + "'s Room")
                .host(host)
                .build();

        // Kiểm tra player có sẵn hay không
        Player hostPlayer = playerRepository.findByUser(host).orElse(null);

        if (hostPlayer == null) {
            hostPlayer = Player.builder()
                    .user(host)
                    .online(true)
                    .host(true)
                    .room(room)
                    .build();
        } else {
            // reset trạng thái nếu player đã tồn tại
            hostPlayer.setOnline(true);
            hostPlayer.setHost(true);
            hostPlayer.leaveRoom();
            hostPlayer.setRoom(room);
        }

        room.getPlayers().add(hostPlayer);
        roomRepository.save(room);
        playerRepository.save(hostPlayer);

        return room;
    }



    @Override
    public Room joinRoom(Long roomId, Long playerId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Nếu player đã ở đúng phòng thì bỏ qua
        if (room.equals(player.getRoom())) return room;

        player.leaveRoom();
        room.addPlayer(player);
        player.setOnline(true);
        player.setHost(false);

        playerRepository.save(player);
        return roomRepository.save(room);
    }


    @Override
    public void leaveRoom(Long roomId, Long playerId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        room.removePlayer(player);
        player.setOnline(false);
        player.setRoom(null);
        playerRepository.save(player);

        // Nếu host rời phòng
        if (player.isHost()) {
            if (!room.getPlayers().isEmpty()) {
                // Chọn player đầu tiên làm host mới
                Player newHost = room.getPlayers().iterator().next();
                newHost.setHost(true);
                playerRepository.save(newHost);
                room.setHost(newHost.getUser());
            } else {
                // Không còn ai → xóa phòng
                stopGameLoop(roomId);
                roomRepository.delete(room);
                return;
            }
        }

        roomRepository.save(room);
    }


    @Override
    public List<PlayerDTO> getPlayerDTOsInRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        return room.getPlayers().stream()
                .map(PlayerDTO::new) // dùng constructor PlayerDTO(Player player)
                .collect(Collectors.toList());
    }


    // ===========================
    // GAME LOOP / START / TICK
    // ===========================
    public void startGame(Room room) {
        if (gameLoops.containsKey(room.getId())) return; // đã chạy

        for (Player player : room.getPlayers()) {
            GameState state = new GameState();
            player.setGameState(state);
        }
        playerRepository.saveAll(room.getPlayers());

        ScheduledFuture<?> loop = scheduler.scheduleAtFixedRate(
                () -> tick(room.getId()), 0, 500, TimeUnit.MILLISECONDS);
        gameLoops.put(room.getId(), loop);
    }


    private void tick(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow();

        for (Player player : room.getPlayers()) {
            GameState state = player.getGameState();
            if (state.isGameOver()) continue;

            synchronized (player) { // thread-safe
                state.tick(); // logic rơi piece, clear line
                player.setGameState(state);
                playerRepository.save(player);
            }

            // Broadcast update
            messagingTemplate.convertAndSend("/topic/room/" + roomId,
                    Map.of(
                            "type", "UPDATE",
                            "playerId", player.getId(),
                            "username", player.getUser().getUsername(),
                            "roomId", roomId,
                            "payload", state
                    )
            );

            if (state.isGameOver()) {
                messagingTemplate.convertAndSend("/topic/room/" + roomId,
                        Map.of(
                                "type", "GAME_OVER",
                                "playerId", player.getId(),
                                "username", player.getUser().getUsername(),
                                "roomId", roomId
                        )
                );
            }
        }
    }

    private void stopGameLoop(Long roomId) {
        ScheduledFuture<?> loop = gameLoops.remove(roomId);
        if (loop != null) loop.cancel(true);
    }

    // ===========================
    // UTILITY
    // ===========================
    public Room findRoomByUsername(String username) {
        return roomRepository.findAll().stream()
                .filter(r -> r.getPlayers().stream().anyMatch(p -> p.getUser().getUsername().equals(username)))
                .findFirst().orElse(null);
    }

}
