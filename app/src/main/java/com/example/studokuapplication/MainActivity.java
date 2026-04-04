package com.example.studokuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class MainActivity extends AppCompatActivity {

    Button easy, medium, hard, daily, leaderboard, custom, achievements;
    SwitchCompat switchTheme;

    TextView txtHistory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemePrefs.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        easy = findViewById(R.id.btnEasy);
        medium = findViewById(R.id.btnMedium);
        hard = findViewById(R.id.btnHard);
        daily = findViewById(R.id.btnDaily);
        leaderboard = findViewById(R.id.btnLeaderboard);
        achievements = findViewById(R.id.btnAchievements);
        custom = findViewById(R.id.btnCustom);
        switchTheme = findViewById(R.id.switchTheme);

        txtHistory = findViewById(R.id.history);

        Statistics.load(this);

        easy.setOnClickListener(v -> openGame("dễ"));
        medium.setOnClickListener(v -> openGame("trung bình"));
        hard.setOnClickListener(v -> openGame("khó"));
        daily.setOnClickListener(v -> openDailyChallenge());
        leaderboard.setOnClickListener(v -> startActivity(new Intent(this, LeaderboardActivity.class)));
        achievements.setOnClickListener(v -> startActivity(new Intent(this, AchievementsActivity.class)));
        custom.setOnClickListener(v -> startActivity(new Intent(this, CustomSudokuActivity.class)));

        switchTheme.setChecked(ThemePrefs.isDarkMode(this));
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemePrefs.setDarkMode(this, isChecked);
            ThemePrefs.applyTheme(this);
            recreate();
        });
    }

    void openGame(String level){
        Intent intent = new Intent(this, SudokuActivity.class);
        intent.putExtra("level",level);
        startActivity(intent);
    }

    void openDailyChallenge() {
        Intent intent = new Intent(this, SudokuActivity.class);
        intent.putExtra("level", "daily");
        intent.putExtra("gameMode", "daily");
        startActivity(intent);
    }
    void showHistory(){

        StringBuilder text = new StringBuilder();

        for(GameHistory h : Statistics.historyList){

            String icon = h.result.equals("thắng") ? "✅" : "❌";

            text.append(icon)
                    .append(" ")
                    .append(h.level.toUpperCase())
                    .append("   ")
                    .append(h.formatDuration())
                    .append("   ")
                    .append(h.playedAt)
                    .append("\n");
        }

        if(text.length()==0){
            text.append("Chưa có lịch sử");
        }

        txtHistory.setText(text.toString());
    }
    @Override
    protected void onResume() {
        super.onResume();
        showHistory();
    }
}
