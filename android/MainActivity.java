package com.muse.snake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {

    private TextView scoreView;
    private SnakeView snakeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(24, 28, 24));

        scoreView = new TextView(this);
        scoreView.setText("得分：0    最高：0");
        scoreView.setTextSize(18);
        scoreView.setTextColor(Color.rgb(220, 230, 210));
        scoreView.setGravity(Gravity.CENTER);
        scoreView.setPadding(0, 28, 0, 20);
        root.addView(scoreView);

        snakeView = new SnakeView(this);
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(snakeView, vp);

        LinearLayout pad = new LinearLayout(this);
        pad.setOrientation(LinearLayout.HORIZONTAL);
        pad.setGravity(Gravity.CENTER);
        pad.setPadding(0, 16, 0, 28);
        pad.addView(dirButton("◀", SnakeView.LEFT));
        pad.addView(dirButton("▲", SnakeView.UP));
        pad.addView(dirButton("▼", SnakeView.DOWN));
        pad.addView(dirButton("▶", SnakeView.RIGHT));
        root.addView(pad);

        setContentView(root);
    }

    private Button dirButton(String label, final int dir) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(22);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.rgb(56, 92, 52));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(180, 130);
        p.setMargins(10, 0, 10, 0);
        b.setLayoutParams(p);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snakeView.setDirection(dir);
            }
        });
        return b;
    }

    void updateScore(int score, int best) {
        scoreView.setText("得分：" + score + "    最高：" + best);
    }

    static class SnakeView extends View {
        static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3;
        private static final int COLS = 20, ROWS = 20;

        private final ArrayList<int[]> snake = new ArrayList<>();
        private final Random random = new Random();
        private final Handler handler = new Handler();
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private int dir = RIGHT;
        private int nextDir = RIGHT;
        private int foodX, foodY;
        private int score = 0;
        private int best = 0;
        private int state = 0; // 0 ready, 1 running, 2 over
        private float downX, downY;

        private final Runnable tick = new Runnable() {
            @Override
            public void run() {
                if (state != 1) return;
                step();
                invalidate();
                handler.postDelayed(this, delay());
            }
        };

        SnakeView(Context context) {
            super(context);
            SharedPreferences sp = context.getSharedPreferences("snake", Context.MODE_PRIVATE);
            best = sp.getInt("best", 0);
            reset();
        }

        private int delay() {
            return Math.max(70, 160 - score * 2);
        }

        private void reset() {
            snake.clear();
            int cx = COLS / 2, cy = ROWS / 2;
            for (int i = 0; i < 4; i++) snake.add(new int[]{cx - i, cy});
            dir = RIGHT;
            nextDir = RIGHT;
            score = 0;
            placeFood();
            report();
        }

        private void placeFood() {
            while (true) {
                foodX = random.nextInt(COLS);
                foodY = random.nextInt(ROWS);
                boolean onSnake = false;
                for (int[] s : snake) {
                    if (s[0] == foodX && s[1] == foodY) { onSnake = true; break; }
                }
                if (!onSnake) return;
            }
        }

        void setDirection(int d) {
            if (state == 0) { start(); return; }
            if (state == 2) return;
            if ((dir == UP && d == DOWN) || (dir == DOWN && d == UP)
                    || (dir == LEFT && d == RIGHT) || (dir == RIGHT && d == LEFT)) return;
            nextDir = d;
        }

        private void start() {
            reset();
            state = 1;
            handler.removeCallbacks(tick);
            handler.postDelayed(tick, delay());
            invalidate();
        }

        private void step() {
            dir = nextDir;
            int[] head = snake.get(0);
            int nx = head[0], ny = head[1];
            if (dir == UP) ny--;
            else if (dir == DOWN) ny++;
            else if (dir == LEFT) nx--;
            else nx++;

            if (nx < 0 || nx >= COLS || ny < 0 || ny >= ROWS || hitsSelf(nx, ny)) {
                state = 2;
                if (score > best) {
                    best = score;
                    getContext().getSharedPreferences("snake", Context.MODE_PRIVATE)
                            .edit().putInt("best", best).apply();
                }
                report();
                return;
            }

            snake.add(0, new int[]{nx, ny});
            if (nx == foodX && ny == foodY) {
                score += 10;
                placeFood();
            } else {
                snake.remove(snake.size() - 1);
            }
            report();
        }

        private boolean hitsSelf(int x, int y) {
            for (int i = 0; i < snake.size() - 1; i++) {
                int[] s = snake.get(i);
                if (s[0] == x && s[1] == y) return true;
            }
            return false;
        }

        private void report() {
            Context c = getContext();
            if (c instanceof MainActivity) ((MainActivity) c).updateScore(score, best);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                downX = e.getX();
                downY = e.getY();
                return true;
            }
            if (e.getAction() == MotionEvent.ACTION_UP) {
                float dx = e.getX() - downX;
                float dy = e.getY() - downY;
                if (Math.abs(dx) < 24 && Math.abs(dy) < 24) {
                    if (state == 0 || state == 2) start();
                    return true;
                }
                if (Math.abs(dx) > Math.abs(dy)) setDirection(dx > 0 ? RIGHT : LEFT);
                else setDirection(dy > 0 ? DOWN : UP);
                return true;
            }
            return super.onTouchEvent(e);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth(), h = getHeight();
            float cell = Math.min(w / (float) COLS, h / (float) ROWS);
            float ox = (w - cell * COLS) / 2f;
            float oy = (h - cell * ROWS) / 2f;

            paint.setColor(Color.rgb(34, 40, 34));
            canvas.drawRect(ox, oy, ox + cell * COLS, oy + cell * ROWS, paint);

            paint.setColor(Color.rgb(46, 54, 46));
            for (int i = 1; i < COLS; i++) {
                canvas.drawLine(ox + i * cell, oy, ox + i * cell, oy + cell * ROWS, paint);
            }
            for (int j = 1; j < ROWS; j++) {
                canvas.drawLine(ox, oy + j * cell, ox + cell * COLS, oy + j * cell, paint);
            }

            paint.setColor(Color.rgb(214, 48, 49));
            canvas.drawCircle(ox + (foodX + 0.5f) * cell, oy + (foodY + 0.5f) * cell, cell * 0.36f, paint);

            for (int i = snake.size() - 1; i >= 0; i--) {
                int[] s = snake.get(i);
                paint.setColor(i == 0 ? Color.rgb(126, 211, 33) : Color.rgb(82, 160, 40));
                float pad = cell * 0.08f;
                RectF r = new RectF(ox + s[0] * cell + pad, oy + s[1] * cell + pad,
                        ox + (s[0] + 1) * cell - pad, oy + (s[1] + 1) * cell - pad);
                canvas.drawRoundRect(r, cell * 0.25f, cell * 0.25f, paint);
            }

            if (state != 1) {
                paint.setColor(Color.argb(170, 0, 0, 0));
                canvas.drawRect(ox, oy, ox + cell * COLS, oy + cell * ROWS, paint);
                paint.setColor(Color.WHITE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(cell * 1.1f);
                String line1 = state == 0 ? "贪 吃 蛇" : "游戏结束";
                canvas.drawText(line1, w / 2f, h / 2f - cell, paint);
                paint.setTextSize(cell * 0.62f);
                String line2 = state == 0 ? "点一下屏幕或方向键开始" : "得分 " + score + " · 点一下再来一局";
                canvas.drawText(line2, w / 2f, h / 2f + cell * 0.6f, paint);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            handler.removeCallbacks(tick);
        }
    }
}
