package pl.derjack.ultimatetictactoeproject.cpu;

import pl.derjack.ultimatetictactoeproject.game.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CpuWorker {
    private Game game;
    private Random random;
    private boolean provenEnd;
    public boolean kupa;
    public int ruchy;

    public CpuWorker() {
        game = new Game();
        random = new RandomXS128();
    }

    public void setGame(Game game) {
        this.game.setGame(game);
    }

    public void clearProvenEnd() {
        provenEnd = false;
    }

    public boolean getProvenEnd() {
        return provenEnd;
    }

    public void selectAndExpand(List<Move> moves, int games, int level) {
        List<Integer> indexes = new ArrayList<>();
        double max = -2 * Move.INFINITY;
        for (int i=0;i<moves.size();i++) {
            Move move = moves.get(i);
            double a,b;
            if (move.terminal) {
                a = move.score / move.games;
                b = 0.0;
                if (a == 0.5) a = 0.25;
            } else if (move.games == 0) {
                a = 0.5;
                b = 0.5;
                a += move.heuristic / 10.0;
                a -= move.virtualLoss / 10.0;
            } else {
                int g = move.games + move.virtualLoss;
                a = move.score / g;
                b = 0.22 * Math.sqrt( Math.sqrt(games) / g);
                a += move.heuristic / (g+1.0);
            }

            if (a + b > max) {
                max = a + b;
                indexes.clear();
                indexes.add(i);
            } else if (a + b == max) {
                indexes.add(i);
            }
        }

        Move move = moves.get(indexes.get(random.nextInt(indexes.size())));
        move.lock.lock();
        move.virtualLoss += Move.VIRTUAL_LOSS;
        if (move.terminal) {
            if (move.score == Move.INFINITY) {
                if (level == 0) {
                    provenEnd = true;
                }
                move.virtualLoss -= Move.VIRTUAL_LOSS;
                move.lock.unlock();
                Move parent = move.parent;
                if (parent != null) {
                    parent.update(true, -Move.INFINITY);
                    parent = parent.parent;
                }
                while (parent != null) {
                    parent.update(false, parent.player == move.player ? 1.0 : 0.0);
                    parent = parent.parent;
                }
            } else {
                boolean allChildrenTerminal = true;
                double maxScore = -Move.INFINITY;
                for (Move m : moves) {
                    maxScore = Math.max(maxScore,m.score);
                    if (!m.terminal) {
                        allChildrenTerminal = false;
                        break;
                    }
                }
                move.virtualLoss -= Move.VIRTUAL_LOSS;
                move.lock.unlock();
                Move parent = move.parent;
                if (allChildrenTerminal) {
                    if (level == 0) {
                        provenEnd = true;
                    }
                    if (maxScore == -Move.INFINITY) {
                        if (parent != null) {
                            parent.update(true, Move.INFINITY);
                            parent = parent.parent;
                        }
                        if (parent != null) {
                            parent.update(true, -Move.INFINITY);
                            parent = parent.parent;
                        }
                        while (parent != null) {
                            parent.update(false, parent.player == move.player ? 0.0 : 1.0);
                            parent = parent.parent;
                        }
                    } else {
                        if (parent != null) {
                            parent.update(true, 0.5);
                            parent = parent.parent;
                        }
                        while (parent != null) {
                            parent.update(false, 0.5);
                            parent = parent.parent;
                        }
                    }
                } else {
                    if (maxScore == -Move.INFINITY) {
                        while (parent != null) {
                            parent.update(false, parent.player == move.player ? 0.0 : 1.0);
                            parent = parent.parent;
                        }
                    } else {
                        while (parent != null) {
                            parent.update(false, 0.5);
                            parent = parent.parent;
                        }
                    }
                }
            }
            return;
        }
        game.makeMove(move.move);
        if (move.games > 0) {
            if (move.children == null) {
                move.children = generateMoves(move);
            }
            move.lock.unlock();
            selectAndExpand(move.children,move.games+1,level+1);
        } else {
            double score = simulateOne(move.player);
            move.score += score;
            move.games++;
            move.virtualLoss -= Move.VIRTUAL_LOSS;
            move.lock.unlock();
            Move parent = move.parent;
            while (parent != null) {
                parent.update(false, parent.player == move.player ? score : 1-score);
                parent = parent.parent;
            }
        }
        game.undoMove();
    }


    private List<Move> generateMoves(Move parent) {
        List<Integer> movesBoard = game.getAvailableMoves();
        List<Move> moves = new ArrayList<>(movesBoard.size());
        for (int moveBoard : movesBoard) {
            moves.add(new Move(parent, game.getCurrentPlayer(), moveBoard));
        }
//        List<Move> moves = game.getAvailableMoves().stream().map(move -> new Move(null, game.getCurrentPlayer(), move)).collect(Collectors.toList());
        for (Move move : moves) {
            int h = game.makeMove(move.move);
            if (game.isOver()) {
                int winner = game.getWinner();
                if (winner == move.player) {
                    move.score = Move.INFINITY;
                } else if (winner == Game.NONE) {
                    move.score = 0.5;
                } else {
                    move.score = -Move.INFINITY;
                }
                move.games = 1;
                move.terminal = true;
            } else {
                move.heuristic = h;
            }
            game.undoMove();
        }
        return moves;
    }

    private Game simulationGame = new Game();

    private double simulateOne(int forPlayer) {
        simulationGame.setGameNoHistory(game);

        while (!simulationGame.isOver()) {
            List<Integer> moves = simulationGame.getAvailableMoves();
            boolean broken = false;
            int size = moves.size();
            for (int i=0;i<size;i++) {
                int m = moves.get(i);
                int h = simulationGame.makeMove(m);
                if (simulationGame.isOver() && simulationGame.getWinner() == 3 - simulationGame.getCurrentPlayer()) {
                    broken = true;
                    break;
                } else if (h > 0) {
                    moves.add(m);
                }
                simulationGame.undoMove();
            }
            if (!broken) {
                simulationGame.makeMoveNoHistory(moves.get(random.nextInt(moves.size())));
            }
        }

        int winner = simulationGame.getWinner();
        if (winner == forPlayer) {
            return 1.0;
        } else if (winner == Game.NONE) {
            return 0.5;
        } else {
            return 0.0;
        }
    }

}
