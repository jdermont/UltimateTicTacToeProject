package pl.derjack.ultimatetictactoeproject.game;

public class GameState {
    public final int player;
    public final int move;
    public final int lastBoard;
    public boolean mainBoardMove;

    public GameState(int player, int move, int lastBoard) {
        this.player = player;
        this.move = move;
        this.lastBoard = lastBoard;
        this.mainBoardMove = false;
    }
}
