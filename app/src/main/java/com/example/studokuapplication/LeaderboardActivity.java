package com.example.studokuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class LeaderboardActivity extends AppCompatActivity {

    TextView txtLeaderboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemePrefs.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        txtLeaderboard = findViewById(R.id.txtLeaderboard);
        Button btnBackMain = findViewById(R.id.btnBackMainLeaderboard);
        btnBackMain.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Statistics.load(this);
        renderLeaderboard();
    }

    void renderLeaderboard() {
        StringBuilder sb = new StringBuilder();
        appendLevel(sb, "dễ");
        appendLevel(sb, "trung bình");
        appendLevel(sb, "khó");
        appendLevel(sb, "tự tạo");

        if (sb.length() == 0) {
            sb.append("Chưa có dữ liệu leaderboard");
        }

        txtLeaderboard.setText(sb.toString());
    }

    void appendLevel(StringBuilder sb, String level) {
        List<GameHistory> wins = Statistics.getLeaderboard(this, level);
        sb.append(level.toUpperCase(Locale.getDefault())).append("\n");

        if (wins.isEmpty()) {
            sb.append("- Chưa có lượt thắng\n\n");
            return;
        }

        int top = Math.min(5, wins.size());
        for (int i = 0; i < top; i++) {
            GameHistory h = wins.get(i);
            sb.append(i + 1)
                    .append(". ")
                    .append(h.formatDuration())
                    .append(" - ")
                    .append(h.playedAt)
                    .append("\n");
        }
        sb.append("\n");
    }
}

