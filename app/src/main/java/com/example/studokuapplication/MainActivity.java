package com.example.studokuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button easy, medium, hard;

    TextView txtHistory;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        easy = findViewById(R.id.btnEasy);
        medium = findViewById(R.id.btnMedium);
        hard = findViewById(R.id.btnHard);

        txtHistory = findViewById(R.id.history);

        easy.setOnClickListener(v -> openGame("dễ"));
        medium.setOnClickListener(v -> openGame("trung bình"));
        hard.setOnClickListener(v -> openGame("khó"));
    }

    void openGame(String level){
        Intent intent = new Intent(this, SudokuActivity.class);
        intent.putExtra("level",level);
        startActivity(intent);
    }
    void showHistory(){

        StringBuilder text = new StringBuilder();

        for(GameHistory h : Statistics.historyList){

            String icon = h.result.equals("Win") ? "✅" : "❌";

            text.append(icon)
                    .append(" ")
                    .append(h.level.toUpperCase())
                    .append("   ")
                    .append(h.time)
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
