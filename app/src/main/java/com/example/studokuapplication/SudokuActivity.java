package com.example.studokuapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class SudokuActivity extends AppCompatActivity {

        GridLayout grid;
        EditText[][] cells = new EditText[9][9];
        int[][] puzzle;
        TextView txtLevel;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_sudoku);
            txtLevel = findViewById(R.id.txtLevel);
            grid = findViewById(R.id.gridSudoku);

            String level = getIntent().getStringExtra("level");
            txtLevel.setText("Level: " + level);
            Random random = new Random();
            int index = random.nextInt(5);
            if(level.equals("easy")){
                puzzle =SudokuData.easy[index];
            }

            if(level.equals("medium")){
                puzzle = SudokuData.medium[index];
            }

            if(level.equals("hard")){
                puzzle = SudokuData.hard[index];
            }

            createBoard();
        }

        void createBoard(){

            for(int i=0;i<9;i++){
                for(int j=0;j<9;j++){

                    EditText cell = new EditText(this);
                    cell.setWidth(110);
                    cell.setHeight(110);
                    cell.setTextSize(18);
                    cell.setGravity(Gravity.CENTER);
                    cell.setInputType(InputType.TYPE_CLASS_NUMBER);
                    cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

                    int row = i;
                    int col = j;

                    if(puzzle[i][j] != 0){
                        cell.setText(String.valueOf(puzzle[i][j]));
                        cell.setEnabled(false);
                    }

                    cell.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {}

                        @Override
                        public void afterTextChanged(Editable s) {
                            String text = s.toString().trim();
                            resetHighlight();
                            if(text.isEmpty()){
                                return;
                            }
                            int num = Integer.parseInt(text);
                            highlightSameNumber(num);
                            CheckResult result = checkValid(row,col,num);
                            if(result.valid){
                                cell.setBackgroundColor(Color.WHITE);
                                if(checkWin()){
                                    Toast.makeText(SudokuActivity.this,
                                            "🎉 Bạn đã thắng Sudoku!",
                                            Toast.LENGTH_LONG).show();
                                }

                            }else{
                                cell.setBackgroundColor(Color.RED);
                                Toast.makeText(SudokuActivity.this, "Sai! Số này đã có ở hàng " +(result.row+1)+" cột "+(result.col+1)
                                        , Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                    cells[i][j] = cell;
                    grid.addView(cell);

                }
            }

        }

    CheckResult checkValid(int row,int col,int num){

        // check row
        for(int i=0;i<9;i++){

            if(i!=col){

                String value = cells[row][i].getText().toString();

                if(!value.equals("")){
                    if(Integer.parseInt(value)==num){
                        return new CheckResult(false,row,i);
                    }
                }

            }

        }

        // check column
        for(int i=0;i<9;i++){

            if(i!=row){

                String value = cells[i][col].getText().toString();

                if(!value.equals("")){
                    if(Integer.parseInt(value)==num){
                        return new CheckResult(false,i,col);
                    }
                }

            }

        }

        // check box
        int startRow = row - row%3;
        int startCol = col - col%3;

        for(int i=startRow;i<startRow+3;i++){
            for(int j=startCol;j<startCol+3;j++){

                if(i!=row || j!=col){

                    String value = cells[i][j].getText().toString();

                    if(!value.equals("")){
                        if(Integer.parseInt(value)==num){
                            return new CheckResult(false,i,j);
                        }
                    }

                }

            }
        }

        return new CheckResult(true,-1,-1);
    }
    boolean checkWin(){

        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){

                String value = cells[i][j].getText().toString();

                if(value.equals("")){
                    return false;
                }

            }
        }

        return true;
    }

    void highlightSameNumber(int num){
            for(int i=0;i<9;i++){
                for(int j=0;j<9;j++) {
                    String value = cells[i][j].getText().toString();
                    if (value.equals("")) {
                        continue;
                    }
                    int cellNum = Integer.parseInt(value);
                    if (cellNum == num) {
                        cells[i][j].setBackgroundColor(Color.YELLOW);
                    }
                }
            }
    }
    void resetHighlight(){
        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){
                cells[i][j].setBackgroundColor(Color.WHITE);
            }
        }
    }

}
