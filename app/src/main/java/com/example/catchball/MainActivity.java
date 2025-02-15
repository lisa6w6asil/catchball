package com.example.catchball;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView scoreLabel, startLabel, timerLabel;
    private ImageView box, orange, pink, black;
    private int frameHeight, boxSize, screenWidth, screenHeight;
    private float boxY;
    private float orangeX, orangeY;
    private float pinkX, pinkY;
    private float blackX, blackY;
    private int score = 0;
    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean action_flg = false;
    private boolean start_flg = false;

    private CountDownTimer countDownTimer; // 追加
    private final long timeLimit = 30000; // 30秒（ミリ秒）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scoreLabel = findViewById(R.id.scoreLabel);
        startLabel = findViewById(R.id.startLabel);
        timerLabel = findViewById(R.id.timerLabel); // タイマー用テキスト
        box = findViewById(R.id.box);
        orange = findViewById(R.id.orange);
        pink = findViewById(R.id.pink);
        black = findViewById(R.id.black);

        // スクリーンサイズを取得
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        // 初期位置設定（画面外）
        orange.setX(-80.0f);
        orange.setY(-80.0f);
        pink.setX(-80.0f);
        pink.setY(-80.0f);
        black.setX(-80.0f);
        black.setY(-80.0f);

        scoreLabel.setText("Score : 0");
        timerLabel.setText("Time : 30");
    }

    public void changePos() {
        boxMove();

        // オレンジボールの移動
        orangeX -= 12;
        if (orangeX < 0) {
            orangeX = screenWidth;
            orangeY = (float) Math.floor(Math.random() * (frameHeight - orange.getHeight()));
        }
        orange.setX(orangeX);
        orange.setY(orangeY);

        // ピンクボールの移動
        pinkX -= 16;
        if (pinkX < 0) {
            pinkX = screenWidth;
            pinkY = (float) Math.floor(Math.random() * (frameHeight - pink.getHeight()));
        }
        pink.setX(pinkX);
        pink.setY(pinkY);

        // 黒トゲの移動
        blackX -= 20;
        if (blackX < 0) {
            blackX = screenWidth;
            blackY = (float) Math.floor(Math.random() * (frameHeight - black.getHeight()));
        }
        black.setX(blackX);
        black.setY(blackY);

        // 当たり判定
        if (hitCheck(orangeX, orangeY, orange)) {
            score += 10;
            orangeX = -10;
        }
        if (hitCheck(pinkX, pinkY, pink)) {
            score += 30;
            pinkX = -10;
        }
        if (hitCheck(blackX, blackY, black)) {
            gameOver();
        }

        // スコア更新
        scoreLabel.setText("Score : " + score);
    }

    public void boxMove() {
        if (action_flg) {
            // 上に移動
            boxY -= 20;
        } else {
            // 下に落ちる
            boxY += 20;
        }

        // 画面外に出ないようにする
        if (boxY < 0) boxY = 0;
        if (boxY > frameHeight - boxSize) boxY = frameHeight - boxSize;

        box.setY(boxY);
    }

    public boolean hitCheck(float x, float y, ImageView object) {
        float objectCenterX = x + object.getWidth() / 2;
        float objectCenterY = y + object.getHeight() / 2;
        float boxCenterX = box.getX() + box.getWidth() / 2;
        float boxCenterY = box.getY() + box.getHeight() / 2;

        float boxHalfWidth = box.getWidth() / 2;
        float boxHalfHeight = box.getHeight() / 2;
        float objectHalfWidth = object.getWidth() / 2;
        float objectHalfHeight = object.getHeight() / 2;

        return (Math.abs(objectCenterX - boxCenterX) < boxHalfWidth + objectHalfWidth) &&
                (Math.abs(objectCenterY - boxCenterY) < boxHalfHeight + objectHalfHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!start_flg) {
            start_flg = true;
            frameHeight = findViewById(R.id.frame).getHeight();
            boxY = box.getY();
            boxSize = box.getHeight();
            startLabel.setVisibility(View.GONE);

            // タイマー開始
            startTimer();

            handler.post(runnable = new Runnable() {
                @Override
                public void run() {
                    changePos();
                    handler.postDelayed(this, 20);
                }
            });
        } else {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                action_flg = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                action_flg = false;
            }
        }
        return true;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLimit, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerLabel.setText("Time : " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                gameOver();
            }
        }.start();
    }

    public void gameOver() {
        handler.removeCallbacks(runnable);
        countDownTimer.cancel();
        Intent intent = new Intent(getApplicationContext(), GameOverActivity.class);
        intent.putExtra("SCORE", score);
        startActivity(intent);
        finish();
    }
}
