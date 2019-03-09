package pl.derjack.ultimatetictactoeproject.cpu;

import pl.derjack.ultimatetictactoeproject.game.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CpuParallel {
    private int player;
    private Game game;
    private List<CpuWorker> cpuWorkers;

    public Move lastMove;
    public int opponentMove;

    public CpuParallel(int numThreads) {
        player = Game.NONE;
        cpuWorkers = new ArrayList<>(numThreads);
        for (int i=0;i<numThreads;i++) {
            cpuWorkers.add(new CpuWorker());
        }
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public int getPlayer() {
        return player;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean kupa;

    public void setKupa() {
        kupa = true;
        for (CpuWorker cpuWorker : cpuWorkers) cpuWorker.kupa = true;
    }

    int ruchy;
    public void setRuchy(int r) {
        ruchy = r;
        for (CpuWorker cpuWorker : cpuWorkers) cpuWorker.ruchy = r;
    }

    public Move getBestMove(final long timeInMillis) {
        final long start = System.currentTimeMillis();
        List<Move> moves = generateMoves();
        if (moves.size() == 1) {
            lastMove = null;
            return moves.get(0);
        }

        final AtomicInteger games = new AtomicInteger();
        if (kupa && lastMove != null) {
            if (lastMove.children != null) {
                for (Move child : lastMove.children) {
                    if (child.move == opponentMove) {
                        if (child.children != null) {
                            moves = child.children;
                            for (Move grandChild : moves) {
                                grandChild.parent = null;
                            }
                            games.set(child.games);
                        }
                        break;
                    }
                }
            }
            lastMove = null;
        }

        final List<Move> movesFinal = moves;

        final AtomicBoolean provenEnd = new AtomicBoolean();
        ExecutorService executor = Executors.newFixedThreadPool(cpuWorkers.size());
        for (int i=0;i<cpuWorkers.size();i++) {
            final CpuWorker cpuWorker = cpuWorkers.get(i);
            cpuWorker.setGame(game);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    cpuWorker.clearProvenEnd();
                    while (!provenEnd.get() && games.get() < timeInMillis) {
                        cpuWorker.selectAndExpand(movesFinal, games.get()+1, 0);
                        games.incrementAndGet();
                        provenEnd.set(provenEnd.get()|cpuWorker.getProvenEnd());
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Collections.shuffle(moves);
        Collections.sort(moves);
        System.out.println("games "+games);
        Move move = Collections.max(moves);
        if (kupa) lastMove = move;
        return move;
    }

//    public void ponder(final long timeInMillis) {
//        final long start = System.currentTimeMillis();
//        if (lastMove == null || lastMove.terminal || lastMove.children == null) {
//            return;
//        }
//
//        final List<Move> movesFinal = lastMove.children;
//        final AtomicInteger games = new AtomicInteger(lastMove.games);
//
//
//        final AtomicBoolean provenEnd = new AtomicBoolean();
//        ExecutorService executor = Executors.newFixedThreadPool(cpuWorkers.size());
//        for (int i=0;i<cpuWorkers.size();i++) {
//            final CpuWorker cpuWorker = cpuWorkers.get(i);
//            cpuWorker.setGame(game);
//            executor.execute(new Runnable() {
//                @Override
//                public void run() {
//                    cpuWorker.clearProvenEnd();
//                    while (!provenEnd.get() && System.currentTimeMillis() - start < timeInMillis) {
//                        cpuWorker.selectAndExpand(movesFinal, games.get()+1, 0);
//                        games.incrementAndGet();
//                        provenEnd.set(provenEnd.get()|cpuWorker.getProvenEnd());
//                    }
//                }
//            });
//        }
//
//        executor.shutdown();
//        try {
//            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    private List<Move> generateMoves() {
        List<Integer> movesBoard = game.getAvailableMoves();
        List<Move> moves = new ArrayList<>(movesBoard.size());
        for (int moveBoard : movesBoard) {
            moves.add(new Move(null, game.getCurrentPlayer(), moveBoard));
        }
//        List<Move> moves = game.getAvailableMoves().stream().map(move -> new Move(null, game.getCurrentPlayer(), move)).collect(Collectors.toList());
        for (Move move : moves) {
            int h = game.makeMove(move.move);
            if (game.isOver()) {
                if (game.getWinner() == move.player) {
                    move.score = Move.INFINITY;
                } else if (game.getWinner() == Game.NONE) {
                    move.score = 0.5;
                } else {
                    move.score = -Move.INFINITY;
                }
                move.games = 1;
                move.terminal = true;
            } else {
                move.heuristic = h;
                //System.out.println("dupa cycki "+h);
            }
            game.undoMove();
        }
        return moves;
    }

}
