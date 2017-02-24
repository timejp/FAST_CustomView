package com.timejh.customview;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    CustomView view;

    FrameLayout ground;
    Button btnUp, btnDown, btnLeft, btnRight;

    // 화면칸수
    private static final int GROUND_SIZE = 10;
    private float deviceWidth = 0;
    // 이동단위
    float unit = 0;

    // 플레이어 정보
    int player_x = 0;
    int player_y = 0;
    float player_radius = 0;

    // 스테이지 정보
    Stage stage;
    // 스테이지 레벨
    int stage_level = 1;
    // 맵 정보 < Stage 에서 맵을 꺼내서 담아주는 변수
    int map[][];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stage = new Stage();

        init(stage_level);

        ground = (FrameLayout) findViewById(R.id.ground);
        btnUp = (Button) findViewById(R.id.btnUp);
        btnDown = (Button) findViewById(R.id.btnDown);
        btnLeft = (Button) findViewById(R.id.btnLeft);
        btnRight = (Button) findViewById(R.id.btnRight);

        btnUp.setOnClickListener(this);
        btnDown.setOnClickListener(this);
        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);

        view = new CustomView(this);
        ground.addView(view);
    }

    private void init(int stage_level) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        // unit 은 화면사이즈 / 그라운드 칸수
        deviceWidth = metrics.widthPixels;
        unit = deviceWidth / GROUND_SIZE;
        // player_radius 는 반지름이므로 unit 을 반으로 나눈다
        player_radius = unit / 2;

        player_x = 0;
        player_y = 0;
        map = stage.getStage(stage_level);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnUp:
                if (player_y > 0 && !collisionCheck("up"))
                    player_y = player_y - 1;
                break;
            case R.id.btnDown:
                if (player_y < GROUND_SIZE - 1 && !collisionCheck("down"))
                    player_y = player_y + 1;
                break;
            case R.id.btnLeft:
                if (player_x > 0 && !collisionCheck("left"))
                    player_x = player_x - 1;
                break;
            case R.id.btnRight:
                if (player_x < GROUND_SIZE - 1 && !collisionCheck("right")) {
                    player_x = player_x + 1;
                }
                break;
        }
        // 화면을 다시그려주는 함수 -> 화면을 지운후에 onDraw를 호출해준다
        view.invalidate();

        // 목표지점에 도달하면 다음 스테이지 또는 앱 종료
        if (map[player_y][player_x] == 2) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Finish");
            dialog.setMessage("Stage Clear!!!");
            dialog.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stage_level++;
                    init(stage_level);
                    view.invalidate();
                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            dialog.show();
        }
    }

    // 충돌검사
    // 진행방향에 장애물이 있는지 검사해준다
    // 장애물이 있으면 true 리턴
    private boolean collisionCheck(String direction) {
        if (direction.equals("up")) {
            if (map[player_y - 1][player_x] == 1) {
                return true;
            }
        } else if (direction.equals("down")) {
            if (map[player_y + 1][player_x] == 1) {
                return true;
            }
        } else if (direction.equals("left")) {
            if (map[player_y][player_x - 1] == 1) {
                return true;
            }
        } else if (direction.equals("right")) {
            if (map[player_y][player_x + 1] == 1) {
                return true;
            }
        }
        return false;
    }

    class CustomView extends View {
        // # onDraw 함수에서 그림그리기
        // 1. 색상을 정의
        Paint magenta = new Paint();
        Paint black = new Paint();
        Paint goal = new Paint();
        Paint mapColor;

        Paint outline = new Paint();

        public CustomView(Context context) {
            super(context);
            magenta.setColor(Color.MAGENTA);
            black.setColor(Color.BLACK);
            goal.setColor(Color.GREEN);
            outline.setStyle(Paint.Style.STROKE);
            outline.setColor(Color.BLACK);
            outline.setStrokeWidth(10);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // 외곽선을 그린다
            canvas.drawRect(0, 0, deviceWidth, deviceWidth, outline);

            // 맵을 화면에 그린다
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    int current = map[i][j];
                    if (current != 0) {
                        switch (map[i][j]) {
                            case 1: // 장애물 박스
                                mapColor = black;
                                break;
                            case 2: // 도착지점
                                mapColor = goal;
                                break;
                        }
                        canvas.drawRect(
                                // 왼쪽 위 꼭지점
                                unit * j, // 왼쪽 위 x
                                unit * i, // 왼쪽 위 y
                                // 오른쪽 아래 꼭지점
                                unit * j + unit, // 오른쪽 아래 x
                                unit * i + unit, // 오른쪽 아래 y
                                mapColor);
                    }
                }
            }

            // 플레이어를 화면에 그린다
            canvas.drawCircle(
                    player_x * unit + player_radius, // 플레이어의 x좌표 : 중심축 기준이므로 반지름값을 더해준다
                    player_y * unit + player_radius, // 플레이어의 y좌표
                    player_radius, // 플레이어의 반지름
                    magenta);
        }
    }
}