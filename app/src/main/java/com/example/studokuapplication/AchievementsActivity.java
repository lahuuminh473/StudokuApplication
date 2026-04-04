package com.example.studokuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AchievementsActivity extends AppCompatActivity {

    TextView txtAchievements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemePrefs.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        txtAchievements = findViewById(R.id.txtAchievementsPage);
        Button btnBackMain = findViewById(R.id.btnBackMainAchievements);
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
        txtAchievements.setText(Statistics.getAchievementSummary(this));
    }
}

