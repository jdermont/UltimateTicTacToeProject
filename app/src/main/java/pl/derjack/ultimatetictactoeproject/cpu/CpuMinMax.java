package pl.derjack.ultimatetictactoeproject.cpu;



import android.util.Log;
import pl.derjack.ultimatetictactoeproject.game.Game;

import java.util.*;
import java.util.stream.Collectors;

public class CpuMinMax {

    private int player;
    private Game game = new Game();

    private Random random;

    public int levels;
    public boolean kupa;
    public int movesNum;

    public CpuMinMax() {
        random = new RandomXS128();
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game.setGame(game);
    }

    public Move getBestMove() {
        tt.clear();
        List<Move> moves = getMoves();
        if (moves.size() == 1) {
            return moves.get(0);
        }

        double score = moves.get(0).score;
        int n = 1;
        for (int i=1; i < moves.size(); i++) {
            if (score > moves.get(i).score) break;
            n++;
        }

        return moves.get(random.nextInt(n));
    }

    final int EXACT = 0;
    final int UPPER = 1;
    final int LOWER = 2;

    static class Pair {
        int flag;
        double score;
    }

    public HashMap<Long, Pair> tt = new HashMap<>();

    private List<Move> getMoves() {
        List<Move> moves = generateMoves();
        Collections.shuffle(moves, random);
        for (Move move : moves) {
            movesNum++;
            game.makeMove(move.move);
            if (game.isOver()) {
                int winner = game.getWinner();
                if (winner == player) move.score = Move.INFINITY + levels;
                else if (winner == Game.NONE) move.score = 0.5;
                else move.score = -Move.INFINITY - levels;
            } else if (levels > 1) {
                move.score = getScore(1,-Move.INFINITY-levels, Move.INFINITY+levels, false);
            } else {
                move.score = game.getCount(player);
            }
            game.undoMove();
        }

        Collections.sort(moves);
        Collections.reverse(moves);

        Log.d("dupa.cycki", moves.toString());
        return moves;
    }

    private double getScore(int level, double alpha, double beta, boolean maximizer) {
        List<Integer> moves = game.getAvailableMoves();
        Collections.shuffle(moves, random);

        double output = -13;

        for (int move : moves) {
            movesNum++;
            double score = 0.0;
            game.makeMove(move);
            boolean terminal = game.isOver();
            if (terminal) {
                int winner = game.getWinner();
                if (winner == player) score = Move.INFINITY - level;
                else if (winner == Game.NONE) score = 0.5;
                else score = -Move.INFINITY + level;
            } else if (level < levels) {
                score = getScore(level+1, alpha, beta, !maximizer);
            } else {
                score = game.getCount(player);
                if (game.isMeh(move)) {
                    score += maximizer ? -0.5 : 0.5;
                }
            }
            game.undoMove();

            if (output == -13) output = score;

            if (maximizer) {
                output = Math.max(score, output);
                alpha = Math.max(output, alpha);
            } else {
                output = Math.min(score, output);
                beta = Math.min(output, beta);
            }

            if (alpha >= beta) {
                return output;
            }
        }

        return output;
    }

    private double getScore2(int level, double alpha, double beta, boolean maximizer) {
        List<Integer> moves = game.getAvailableMoves();
        Collections.shuffle(moves, random);

        double output = -13;

        for (int move : moves) {
            movesNum++;
            double score = 0.0;
            game.makeMove(move);
            if (game.isOver()) {
                int winner = game.getWinner();
                if (winner == player) score = Move.INFINITY - level;
                else if (winner == Game.NONE) score = 0.5;
                else score = -Move.INFINITY + level;
            } else if (level < levels-4) {
                score = getScore(level+1, alpha, beta, !maximizer);
            } else {
                score = game.getCount(player);
            }
            game.undoMove();

            if (output == -13) output = score;

            if (maximizer) {
                output = Math.max(score, output);
                alpha = Math.max(output, alpha);
            } else {
                output = Math.min(score, output);
                beta = Math.min(output, beta);
            }

            if (alpha >= beta) {
                return output;
            }
        }

        return output;
    }

    private List<Move> generateMoves() {
        List<Integer> movesBoard = game.getAvailableMoves();
        List<Move> moves = new ArrayList<>(movesBoard.size());
        for (int moveBoard : movesBoard) {
            moves.add(new Move(null, game.getCurrentPlayer(), moveBoard));
        }
        return moves;
    }
}
