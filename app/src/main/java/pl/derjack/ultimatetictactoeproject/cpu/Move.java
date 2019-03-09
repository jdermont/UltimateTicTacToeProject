package pl.derjack.ultimatetictactoeproject.cpu;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Move implements Comparable<Move> {
    public static final double INFINITY = 1e9;
    public static final int VIRTUAL_LOSS = 1;

    public Move parent;
    public List<Move> children;
    public final int player;
    public final int move;
    public final Lock lock;

    public double score;
    public int games;
    public boolean terminal;
    public int virtualLoss;

    public int heuristic;

    public Move(Move parent, int player, int move) {
        this.parent = parent;
        this.player = player;
        this.move = move;
        this.lock = new ReentrantLock();
    }

    public void update(boolean term, double sc) {
        lock.lock();
        if (!terminal) {
            if (term) {
                if (sc == 0.5) {
                    games++;
                    score = games / 2.0;
                } else {
                    score = sc;
                    games++;
                }
                terminal = true;
            } else {
                score += sc;
                games++;
            }
        }
        virtualLoss -= VIRTUAL_LOSS;
        lock.unlock();
    }

    @Override
    public int compareTo(Move o) {
        double avg1 = score / (games+1e-3);
        double avg2 = o.score / (o.games+1e-3);
        return Double.compare(avg1,avg2);
    }

    @Override
    public String toString() {
        return move+": "+score+"/"+games+" "+terminal;
    }
}
