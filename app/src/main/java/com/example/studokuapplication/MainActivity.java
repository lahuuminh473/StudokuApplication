package com.example.studokuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button easy, medium, hard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        easy = findViewById(R.id.btnEasy);
        medium = findViewById(R.id.btnMedium);
        hard = findViewById(R.id.btnHard);

        easy.setOnClickListener(v -> openGame("easy"));
        medium.setOnClickListener(v -> openGame("medium"));
        hard.setOnClickListener(v -> openGame("hard"));
    }

    void openGame(String level){
        Intent intent = new Intent(this, SudokuActivity.class);
        intent.putExtra("level",level);
        startActivity(intent);
    }
}
