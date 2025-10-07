package kienminh.tetrisgame.service.interfaces;
import kienminh.tetrisgame.dto.UserDTO;
import kienminh.tetrisgame.model.entity.User;

public interface AuthService {
    UserDTO register(String username, String password);
    String login(String username, String password);
    UserDTO getCurrentUser();
    User getAuthenticatedUser();
}
