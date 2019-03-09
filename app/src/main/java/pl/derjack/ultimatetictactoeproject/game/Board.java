package pl.derjack.ultimatetictactoeproject.game;

import java.util.ArrayList;
import java.util.List;

// bitboard idea taken from http://libfbp.blogspot.com/2017/05/tic-tac-toe-bitboards.html
public class Board {
    private static final int BOARD_SIZE = 3;

    private static final int[] wins = {
            0b0000000000000111,
            0b0000000000111000, // 3 rows
            0b0000000111000000,
            0b0000000100100100,
            0b0000000010010010, // 3 columns
            0b0000000001001001,
            0b0000000100010001, // 2 diagonals
            0b0000000001010100
    };

    public static int coordsToMove(int column, int row) {
        return BOARD_SIZE*row + column;
    }

    public static int[] moveToCoords(int move) {
        return new int[] { move % BOARD_SIZE, move / BOARD_SIZE };
    }

    private int gameState;
    private int moveCount;
//    private int winner;

    public Board() {

    }

    public void setBoard(Board board) {
        this.gameState = board.gameState;
        this.moveCount = board.moveCount;
//        this.winner = board.winner;
    }

    public void makeMove(int player, int move) {
        if (player == Game.X) {
            gameState |= 1 << move;
        } else {
            gameState |= 1 << (16 + move);
        }
        moveCount++;
    }

    public void undoMove(int player, int move) {
        if (player == Game.X) {
            gameState &= ~(1 << move);
        } else {
            gameState &= ~(1 << (16 + move));
        }
        moveCount--;
    }

    private static final List<List<Integer>> movesList = new ArrayList<>();
    static {
        for (int i=0;i<512;i++) {
            List<Integer> moves = new ArrayList<>();
            if ((i&1) == 0) moves.add(0);
            if ((i&2) == 0) moves.add(1);
            if ((i&4) == 0) moves.add(2);
            if ((i&8) == 0) moves.add(3);
            if ((i&16) == 0) moves.add(4);
            if ((i&32) == 0) moves.add(5);
            if ((i&64) == 0) moves.add(6);
            if ((i&128) == 0) moves.add(7);
            if ((i&256) == 0) moves.add(8);
            movesList.add(moves);
        }
    }
    public List<Integer> getMoves() {
        int board = (gameState|(gameState>>>16))&0x01FF;
        return movesList.get(board);
    }

    public boolean isOver() {
        return moveCount == 9 || (moveCount > 2 && getWinner() != Game.NONE);
    }

    public void makeEmptyMove() {
        moveCount++;
    }

    public void undoEmptyMove() {
        moveCount--;
    }

//    private int createWinner() {
//        int xState = gameStates&0x01FF;
//        int oState = (gameStates>>>16)&0x01FF;
//        for (int win : wins) {
//            if ((win & xState) == win) {
//                winner = Game.X;
//                return winner;
//            } else if ((win & oState) == win) {
//                winner = Game.O;
//                return winner;
//            }
//        }
//        winner = Game.NONE;
//        return winner;
//    }

    public int getWinner() {
        int xState = gameState&0x01FF;
        int oState = (gameState>>>16)&0x01FF;
        for (int win : wins) {
            if ((win & xState) == win) {
                return Game.X;
            } else if ((win & oState) == win) {
                return Game.O;
            }
        }
        return Game.NONE;
    }

    public int getGameState() {
        return gameState;
    }

    public int getMoveCount() {
        return moveCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<9;i++) {
            if (i >0 && i%3 == 0) sb.append('\n');
            if ((gameState&(1<<i)) != 0) sb.append('X');
            else if ((gameState&(1<<(16+i))) != 0) sb.append('O');
            else sb.append('.');
        }
        return sb.toString();
    }
}