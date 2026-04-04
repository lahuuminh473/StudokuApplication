package com.example.studokuapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CustomSudokuActivity extends AppCompatActivity {

    GridLayout grid;
    EditText[][] cells = new EditText[9][9];
    TextView txtCustomError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemePrefs.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_sudoku);

        grid = findViewById(R.id.gridCustom);
        Button btnPlay = findViewById(R.id.btnPlayCustom);
        Button btnClear = findViewById(R.id.btnClearCustom);
        Button btnBackMain = findViewById(R.id.btnBackMainCustom);
        txtCustomError = findViewById(R.id.txtCustomError);

        createEditorBoard();

        btnClear.setOnClickListener(v -> clearBoard());
        btnPlay.setOnClickListener(v -> startCustomGame());
        btnBackMain.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    void createEditorBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                EditText cell = new EditText(this);
                cell.setWidth(95);
                cell.setHeight(95);
                cell.setTextSize(18);
                cell.setGravity(Gravity.CENTER);
                cell.setTextColor(ContextCompat.getColor(this, R.color.sudoku_text));
                cell.setInputType(InputType.TYPE_CLASS_NUMBER);
                cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                cell.setBackgroundResource(R.drawable.cell_border);
                cell.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        validateBoardLive();
                    }
                });

                cells[i][j] = cell;
                grid.addView(cell);
            }
        }
    }

    void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setText("");
            }
        }
        validateBoardLive();
    }

    void startCustomGame() {
        if (!validateBoardLive()) {
            Toast.makeText(this, "Đề đang lỗi, vui lòng sửa ô màu đỏ", Toast.LENGTH_SHORT).show();
            return;
        }

        int[][] board = new int[9][9];
        int clues = 0;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String value = cells[i][j].getText().toString();
                if (value.isEmpty()) {
                    board[i][j] = 0;
                } else {
                    int num = Integer.parseInt(value);
                    if (num < 1 || num > 9) {
                        Toast.makeText(this, "Chỉ được nhập từ 1-9", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    board[i][j] = num;
                    clues++;
                }
            }
        }

        if (clues < 17) {
            Toast.makeText(this, "Đề nên có ít nhất 17 số gợi ý", Toast.LENGTH_SHORT).show();
            return;
        }

        SudokuData.PuzzleData custom = SudokuData.fromCustom(board);
        if (custom == null) {
            Toast.makeText(this, "Đề không hợp lệ hoặc không có nghiệm", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, SudokuActivity.class);
        intent.putExtra("level", "tự tạo");
        intent.putExtra("customPuzzle", SudokuData.serializeBoard(custom.puzzle));
        startActivity(intent);
        finish();
    }

    boolean validateBoardLive() {
        boolean[][] invalid = new boolean[9][9];
        int[][] board = new int[9][9];
        boolean hasError = false;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String value = cells[i][j].getText().toString().trim();
                if (value.isEmpty()) {
                    board[i][j] = 0;
                    continue;
                }

                int num;
                try {
                    num = Integer.parseInt(value);
                } catch (NumberFormatException ex) {
                    num = -1;
                }

                if (num < 1 || num > 9) {
                    invalid[i][j] = true;
                    hasError = true;
                    board[i][j] = 0;
                } else {
                    board[i][j] = num;
                }
            }
        }

        for (int row = 0; row < 9; row++) {
            for (int col1 = 0; col1 < 9; col1++) {
                int val = board[row][col1];
                if (val == 0) {
                    continue;
                }
                for (int col2 = col1 + 1; col2 < 9; col2++) {
                    if (board[row][col2] == val) {
                        invalid[row][col1] = true;
                        invalid[row][col2] = true;
                        hasError = true;
                    }
                }
            }
        }

        for (int col = 0; col < 9; col++) {
            for (int row1 = 0; row1 < 9; row1++) {
                int val = board[row1][col];
                if (val == 0) {
                    continue;
                }
                for (int row2 = row1 + 1; row2 < 9; row2++) {
                    if (board[row2][col] == val) {
                        invalid[row1][col] = true;
                        invalid[row2][col] = true;
                        hasError = true;
                    }
                }
            }
        }

        for (int boxRow = 0; boxRow < 9; boxRow += 3) {
            for (int boxCol = 0; boxCol < 9; boxCol += 3) {
                for (int i1 = 0; i1 < 9; i1++) {
                    int r1 = boxRow + i1 / 3;
                    int c1 = boxCol + i1 % 3;
                    int val = board[r1][c1];
                    if (val == 0) {
                        continue;
                    }
                    for (int i2 = i1 + 1; i2 < 9; i2++) {
                        int r2 = boxRow + i2 / 3;
                        int c2 = boxCol + i2 % 3;
                        if (board[r2][c2] == val) {
                            invalid[r1][c1] = true;
                            invalid[r2][c2] = true;
                            hasError = true;
                        }
                    }
                }
            }
        }

        int normalColor = ContextCompat.getColor(this, R.color.sudoku_text);
        int errorColor = ContextCompat.getColor(this, R.color.mistake_text);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setTextColor(invalid[i][j] ? errorColor : normalColor);
            }
        }

        if (hasError) {
            txtCustomError.setVisibility(View.VISIBLE);
            txtCustomError.setText("Đề đang lỗi: trùng số hoặc giá trị không hợp lệ");
        } else {
            txtCustomError.setVisibility(View.GONE);
        }

        return !hasError;
    }
}

