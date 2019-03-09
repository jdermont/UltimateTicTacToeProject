package pl.derjack.ultimatetictactoeproject.game;

import java.util.ArrayList;
import java.util.List;

public class Game {
    public static final int NONE = 0;
    public static final int X = 1;
    public static final int O = 2;
    private static final int ANY_BOARD = -1;

    public Board[] boards;
    private Board mainBoard;

    private int currentPlayer;
    private int lastBoard;

    private List<GameState> gameStates;

    public static int coordsToMove(int col, int row) {
        int main = 0;
        switch (row) {
            case 0:
            case 1:
            case 2:
                if (col < 3) main = 0;
                else if (col < 6) main = 1;
                else main = 2;
                break;
            case 3:
            case 4:
            case 5:
                if (col < 3) main = 3;
                else if (col < 6) main = 4;
                else main = 5;
                break;
            case 6:
            case 7:
            case 8:
                if (col < 3) main = 6;
                else if (col < 6) main = 7;
                else main = 8;
                break;
        }

        return main * 9 + Board.coordsToMove(col % 3, row % 3);
    }

    public static int[] moveToCoords(int move) {
        int c = 0, r = 0;
        int main = move / 9;
        switch (main) {
            case 0:
                c = 0;
                r = 0;
                break;
            case 1:
                c = 3;
                r = 0;
                break;
            case 2:
                c = 6;
                r = 0;
                break;
            case 3:
                c = 0;
                r = 3;
                break;
            case 4:
                c = 3;
                r = 3;
                break;
            case 5:
                c = 6;
                r = 3;
                break;
            case 6:
                c = 0;
                r = 6;
                break;
            case 7:
                c = 3;
                r = 6;
                break;
            case 8:
                c = 6;
                r = 6;
                break;
        }

        int[] smallCoords = Board.moveToCoords(move % 9);

        return new int[]{c + smallCoords[0], r + smallCoords[1]};
    }

    public Game() {
        boards = new Board[9];
        for (int i = 0; i < boards.length; i++) {
            boards[i] = new Board();
        }
        mainBoard = new Board();
        currentPlayer = Game.X;
        lastBoard = ANY_BOARD;
        gameStates = new ArrayList<>();
    }

    public void setGame(Game game) {
        for (int i = 0; i < boards.length; i++) {
            boards[i].setBoard(game.boards[i]);
        }
        mainBoard.setBoard(game.mainBoard);
        currentPlayer = game.currentPlayer;
        lastBoard = game.lastBoard;
        gameStates = new ArrayList<>(game.gameStates);
    }

    public void setGameNoHistory(Game game) {
        for (int i = 0; i < boards.length; i++) {
            boards[i].setBoard(game.boards[i]);
        }
        mainBoard.setBoard(game.mainBoard);
        currentPlayer = game.currentPlayer;
        lastBoard = game.lastBoard;
        gameStates.clear();
    }

    public void makeMoveNoHistory(int move) {
        Board board = boards[move / 9];
        board.makeMove(currentPlayer, move % 9);
        if (board.isOver()) {
            if (board.getWinner() == currentPlayer) {
                mainBoard.makeMove(currentPlayer, move / 9);
            } else {
                mainBoard.makeEmptyMove();
            }
        }
        if (boards[move % 9].isOver()) {
            lastBoard = ANY_BOARD;
        } else {
            lastBoard = move % 9;
        }
        currentPlayer = 3 - currentPlayer;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isOver() {
        return mainBoard.isOver();
    }

    public int getWinner() {
        return mainBoard.getWinner();
    }

    public int getCount(int player) {
        int gameState = mainBoard.getGameState();
        int xState = gameState & 0x1FF;
        int oState = (gameState >>> 9) & 0x1FF;
        int X = 0;
        int O = 0;

        while (xState != 0) {
            xState &= xState - 1;
            X++;
        }
        while (oState != 0) {
            oState &= oState - 1;
            O++;
        }

        if (player == Game.X) return X - O;
        return O - X;
    }

    public void changePlayer() {
        currentPlayer = 3 - currentPlayer;
    }

    public int getLastMove() {
        if (!gameStates.isEmpty()) {
            return gameStates.get(gameStates.size()-1).move;
        }
        return -1;
    }
    public List<Integer> getAvailableMoves() {
        if (lastBoard == ANY_BOARD) {
            List<Integer> moves = new ArrayList<>(81);
            for (int i = 0; i < boards.length; i++) {
                Board board = boards[i];
                if (!board.isOver()) {
                    List<Integer> boardMoves = new ArrayList<>(board.getMoves());
                    for (int j=0; j < boardMoves.size(); j++) {
                        moves.add(boardMoves.get(j) + 9 * i);
                    }
                }
            }
            return moves;
        } else {
            Board board = boards[lastBoard];
            List<Integer> moves = new ArrayList<>(board.getMoves());
            for (int i=0; i < moves.size(); i++) {
                moves.set(i, moves.get(i) + 9 * lastBoard);
            }
            return moves;
        }
    }

    public boolean wouldOver(int move) {
        Board board = boards[move / 9];
        board.makeMove(currentPlayer, move % 9);
        boolean over = board.isOver();
        board.undoMove(currentPlayer, move % 9);
        return over;
    }

    public boolean isMeh(int move) {
        return boards[move / 9].isOver();
    }

    public int makeMove(int move) {
        GameState gameState = new GameState(currentPlayer, move, lastBoard);
        Board board = boards[move / 9];
        board.makeMove(currentPlayer, move % 9);
        int output = 0;
        if (board.isOver()) {
            gameState.mainBoardMove = true;
            if (board.getWinner() == currentPlayer) {
                output = 2;
                mainBoard.makeMove(currentPlayer, move / 9);
            } else {
                mainBoard.makeEmptyMove();
            }
        }
        if (boards[move % 9].isOver()) {
            lastBoard = ANY_BOARD;
            output -= 1;
        } else {
            lastBoard = move % 9;
        }
        gameStates.add(gameState);
        currentPlayer = 3 - currentPlayer;
        return output;
    }

    public void undoMove() {
        GameState gameState = gameStates.remove(gameStates.size() - 1);
        Board board = boards[gameState.move / 9];
        if (gameState.mainBoardMove) {
            if (board.getWinner() == Game.NONE) {
                mainBoard.undoEmptyMove();
            } else {
                mainBoard.undoMove(gameState.player, gameState.move / 9);
            }
        }
        board.undoMove(gameState.player, gameState.move % 9);
        lastBoard = gameState.lastBoard;
        currentPlayer = 3 - currentPlayer;
    }

    private static long murmurHash3(long x) {
        x ^= x >>> 33;
        x *= 0xff51afd7ed558ccdL;
        x ^= x >>> 33;
        x *= 0xc4ceb9fe1a85ec53L;
        x ^= x >>> 33;

        return x;
    }

    public long getHashLong() {
        long hash = 0L;
        for (int i = 0; i < boards.length; i++) {
            hash ^= murmurHash3((1L << (32 + i)) + boards[i].getGameState());
        }
        hash ^= murmurHash3((9L << 42L) + mainBoard.getGameState());
        hash ^= mainBoard.getMoveCount();
        hash ^= murmurHash3(lastBoard == ANY_BOARD ? Long.MIN_VALUE : (lastBoard << 16));
        return hash;
    }

    public int[][] getMatrix() {
        int[][] matrix = new int[9][9];
        for (int i=0; i < 9; i++) {
            int gameState = boards[i].getGameState();
            for (int j=0; j < 9; j++) {
                int n = 0;
                if ((gameState & (1 << j)) != 0) {
                    n = 1;
                } else if ((gameState & (1 << (j+16))) != 0) {
                    n = 2;
                }
                matrix[j/3 + 3 * (i/3)][j%3 + 3*(i%3)] = n;
            }
        }

        return matrix;
    }

    public int[][] getBigMatrix() {
        int[][] matrix = new int[3][3];
        for (int i=0; i < 9; i++) {
            int gameState = mainBoard.getGameState();
            int n = 0;
            if ((gameState & (1 << i)) != 0) {
                n = 1;
            } else if ((gameState & (1 << (i+16))) != 0) {
                n = 2;
            }
            matrix[i/3][i%3] = n;
        }

        return matrix;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Board board : boards) {
            sb.append(board.toString());
        }
        sb.append(mainBoard.toString()).append(lastBoard);
        return sb.toString();
    }
}