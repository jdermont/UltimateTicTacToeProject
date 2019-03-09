package pl.derjack.ultimatetictactoeproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import pl.derjack.ultimatetictactoeproject.cpu.CpuMinMax;
import pl.derjack.ultimatetictactoeproject.cpu.CpuParallel;
import pl.derjack.ultimatetictactoeproject.cpu.Move;
import pl.derjack.ultimatetictactoeproject.game.Game;

import java.util.Arrays;
import java.util.List;

public class BoardView extends View {
    private static final String TAG = BoardView.class.getSimpleName();

    @ColorInt
    private static final int BACKGROUND_COLOR = 0xFFF0F0F0;
    @ColorInt
    private static final int SMALL_LINE_COLOR = 0xFFB0B0C0;

    private static final float MARGIN = 8;
    private float density;

    private Game game;
    private CpuParallel cpu;

    private Paint paint;

    private float lastWidth;
    private float lastHeight;
    private float block;
    private float marginWidth;
    private float marginHeight;

    public BoardView(Context context) {
        super(context);
        init();
    }

    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = getContext().getResources().getDisplayMetrics().density;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        game = new Game();
        cpu = new CpuParallel(4);
        cpu.setGame(game);
        cpu.setPlayer(Game.O);
        cpu.getBestMove(100L);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw()");

        measureDimensions();
        drawBackground(canvas);

        if (game.getLastMove() != -1) {
            int[] c = Game.moveToCoords(game.getLastMove());
            int row = c[0]; int col = c[1];
            paint.setColor(Color.YELLOW);
            canvas.drawRect(marginWidth + row * block, marginHeight + col * block, marginWidth + (row + 1) * block, marginHeight + (col + 1) * block, paint);
        }

        drawBlocks(canvas);
        drawBigBlocks(canvas);


    }

    private void measureDimensions() {
        if (getWidth() == lastWidth || getHeight() == lastHeight) {
            return;
        }
        Log.d(TAG, "measureDimensions()");

        float size;
        if (getWidth() >= getHeight()) {
            size = getHeight();
            marginHeight = convertToPixels(MARGIN);
            block = (size - 2 * marginHeight) / 9f;
            marginWidth = (getWidth() - 9f * block) / 2f;
        } else {
            size = getWidth();
            marginWidth = convertToPixels(MARGIN);
            block = (size - 2 * marginWidth) / 9f;
            marginHeight = (getHeight() - 9f * block) / 2f;
        }

        lastWidth = getWidth();
        lastHeight = getHeight();
    }

    private float convertToPixels(float dp) {
        return density * dp;
    }

    private void drawBackground(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(BACKGROUND_COLOR);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        paint.setColor(SMALL_LINE_COLOR);
        paint.setStrokeWidth(Math.round(convertToPixels(density)));
        for (int i = 1; i < 9; i++) {
            canvas.drawLine(marginWidth + i * block, marginHeight, marginWidth + i * block, getHeight() - marginHeight, paint);
            canvas.drawLine(marginWidth, marginHeight + i * block, getWidth() - marginWidth, marginHeight + i * block, paint);
        }

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(Math.round(convertToPixels(2f * density)));
        for (int i = 3; i < 9; i += 3) {
            canvas.drawLine(marginWidth + i * block, marginHeight, marginWidth + i * block, getHeight() - marginHeight, paint);
            canvas.drawLine(marginWidth, marginHeight + i * block, getWidth() - marginWidth, marginHeight + i * block, paint);
        }
        drawPossibleMoves(canvas);
    }

    private void drawBlocks(Canvas canvas) {
        int[][] board = game.getMatrix();
        for (int col = 0; col < board.length; col++) {
            for (int row = 0; row < board[col].length; row++) {
                drawBlock(canvas, col, row, board[col][row]);
            }
        }
    }

    private void drawBlock(Canvas canvas, int col, int row, int player) {
        paint.setStrokeWidth(Math.round(convertToPixels(density)));
        paint.setStyle(Paint.Style.STROKE);

        if (player == Game.X) {
            paint.setColor(Color.RED);
            canvas.drawLine(marginWidth + row * block + Math.round(convertToPixels(2f * density)), marginHeight + col * block + Math.round(convertToPixels(2f * density)),
                    marginWidth + (row + 1) * block - Math.round(convertToPixels(2f * density)), marginHeight + (col + 1) * block - Math.round(convertToPixels(2f * density)), paint);
            canvas.drawLine(marginWidth + (row + 1) * block - Math.round(convertToPixels(2f * density)), marginHeight + col * block + Math.round(convertToPixels(2f * density)),
                    marginWidth + row * block + Math.round(convertToPixels(2f * density)), marginHeight + (col + 1) * block - Math.round(convertToPixels(2f * density)), paint);
        } else if (player == Game.O) {
            paint.setColor(Color.BLUE);
//            canvas.drawArc(marginWidth + row * block + Math.round(convertToPixels(2f * density)), marginHeight + col * block + Math.round(convertToPixels(2f * density)),
//                    marginWidth + (row + 1) * block - Math.round(convertToPixels(2f * density)), marginHeight + (col + 1) * block - Math.round(convertToPixels(2f * density)),
//                    0, 180, false, paint);
            canvas.drawCircle(marginWidth + row * block + block / 2f, marginHeight + col * block + block / 2f, (block / 2f) - Math.round(convertToPixels(2f * density)), paint);
        }
    }

    private void drawBigBlocks(Canvas canvas) {
        int[][] board = game.getBigMatrix();
        for (int col = 0; col < board.length; col++) {
            for (int row = 0; row < board[col].length; row++) {
                drawBigBlock(canvas, col, row, board[col][row]);
            }
        }
    }

    private void drawBigBlock(Canvas canvas, int col, int row, int player) {
        paint.setStrokeWidth(Math.round(convertToPixels(density)));
        paint.setStyle(Paint.Style.STROKE);

        if (player == Game.X) {
            drawBigRect(canvas, col, row);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.RED);
            canvas.drawLine(marginWidth + row * 3 * block + Math.round(convertToPixels(2f * density)), marginHeight + col * 3 * block + Math.round(convertToPixels(2f * density)),
                    marginWidth + (row + 1) * 3 * block - Math.round(convertToPixels(2f * density)), marginHeight + (col + 1) * 3 * block - Math.round(convertToPixels(2f * density)), paint);
            canvas.drawLine(marginWidth + (row + 1) * 3 * block - Math.round(convertToPixels(2f * density)), marginHeight + col * 3 * block + Math.round(convertToPixels(2f * density)),
                    marginWidth + row * 3 * block + Math.round(convertToPixels(2f * density)), marginHeight + (col + 1) * 3 * block - Math.round(convertToPixels(2f * density)), paint);
        } else if (player == Game.O) {
            drawBigRect(canvas, col, row);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLUE);
            canvas.drawCircle(marginWidth + row * 3 * block + 3 * block / 2f, marginHeight + col * 3 * block + 3 * block / 2f, (3 * block / 2f) - Math.round(convertToPixels(2f * density)), paint);
        }
    }

    private void drawBigRect(Canvas canvas, int col, int row) {
        paint.setColor(0xF0FFFFFF);
        paint.setStyle(Paint.Style.FILL);
        float margin = Math.round(convertToPixels(density));
        float marginLeft = row == 0 ? 0 : margin;
        float marginTop = col == 0 ? 0 : margin;
        float marginRight = row == 2 ? 0 : margin;
        float marginBottom = col == 2 ? 0 : margin;
        canvas.drawRect(marginWidth + row * 3 * block + marginLeft, marginHeight + col * 3 * block + marginTop,
                marginWidth + (row + 1) * 3 * block - marginRight, marginHeight + (col + 1) * 3 * block - marginBottom, paint);
    }

    private void drawPossibleMoves(Canvas canvas) {
        if (game.isOver()) return;
        List<Integer> moves = game.getAvailableMoves();

        for (int move : moves) {
            int[] c = Game.moveToCoords(move);
            int col = c[1]; int row = c[0];
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(marginWidth + row * block + block / 2f, marginHeight + col * block + block / 2f, block / 8f, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, event.getX()+" "+event.getY());
        float x = event.getX();
        float y = event.getY();
        int col = (int)((x - marginWidth) / block);
        int row = (int)((y - marginHeight) / block);
        if (!game.isOver())
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        if (col >= 0 && col < 9 && row >= 0 && row < 9) {
            int move = Game.coordsToMove(col, row);
            if (game.getAvailableMoves().contains(move)) {
                game.makeMove(move);
                invalidate();
                if (!game.isOver())
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        cpu.setGame(game);
                        final Move move = cpu.getBestMove(100L);
                        Log.d(TAG,move.move+" "+move.score+" "+move.games);
                        game.makeMove(move.move);
                        post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), String.valueOf(move.score/move.games), Toast.LENGTH_SHORT).show();
                                invalidate();
                            }
                        });
                    }
                }).start();
            }
        }
        Log.d(TAG, col+" "+row);
        return super.onTouchEvent(event);
    }
}
