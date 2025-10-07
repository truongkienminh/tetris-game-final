package kienminh.tetrisgame.service.interfaces;

import kienminh.tetrisgame.model.game.Board;

public interface BoardService {
    boolean spawnBlock(Board board);
    boolean moveDown(Board board);
    boolean moveLeft(Board board);
    boolean moveRight(Board board);
    void rotate(Board board);
    void drop(Board board);
    int clearLines(Board board);
}
