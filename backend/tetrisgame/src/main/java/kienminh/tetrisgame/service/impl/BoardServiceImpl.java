package kienminh.tetrisgame.service.impl;

import kienminh.tetrisgame.model.game.Board;
import kienminh.tetrisgame.service.interfaces.BoardService;
import org.springframework.stereotype.Service;

@Service
public class BoardServiceImpl implements BoardService {

    @Override public boolean spawnBlock(Board board) { return board.spawnBlock(); }
    @Override public boolean moveDown(Board board) { return board.moveDown(); }
    @Override public boolean moveLeft(Board board) { return board.moveLeft(); }
    @Override public boolean moveRight(Board board) { return board.moveRight(); }
    @Override public void rotate(Board board) { board.rotateBlock(); }
    @Override public void drop(Board board) { board.dropDown(); }
    @Override public int clearLines(Board board) { return board.clearLines(); }
}
